import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Random
import groovy.json.JsonSlurper
import com.opencsv.CSVWriter
import org.jsoup.select.Elements
import java.time.format.DateTimeParseException

class Core {

    private final String BASE_URL = 'https://www.gov.br/ans/pt-br'
    private final HttpClient httpClient


    Core() {
        this.httpClient = HttpClient.newHttpClient()
    }

    def getResult() {

        def acessoPagina = receberPagina(BASE_URL)

        taskBaixarArquivoTISSAtualizado(acessoPagina)
        taskBaixarTabelaHistoricoVersoes(acessoPagina)
        taskTabelasRelacionadas(acessoPagina)



    }

    private String receberPagina(String url){
        try{
            def request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()

            def response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            println "acessando a $url"
            return response.body()

        }catch(Exception e){
            return "Erro: $e.message"
        }
    }

    private String processarUrl(String url){
        if(!url) return ""

        if(url.startsWith('http')) return url

        return (url.startsWith('/') ? BASE_URL : "$BASE_URL/") + url
    }

    private File baixarArquivo(String url, String nomeArquivo) {
        try {

            nomeArquivo = nomeArquivo.replace(' ', '_')
            
            def pastaDownloads = new File("Downloads")
            if (!pastaDownloads.exists()) {
                if (!pastaDownloads.mkdirs()) {
                    println "Erro ao criar a pasta Downloads"
                    return null
                }
            }
            
            def arquivoDestino = new File(pastaDownloads, nomeArquivo)
            
            def conexao = new URL(url).openConnection()
            conexao.setRequestProperty("User-Agent", "Mozilla/5.0")
            
            conexao.inputStream.withStream { input ->
                arquivoDestino.withOutputStream { output ->
                    output << input
                }
            }
            
            if (arquivoDestino.exists() && arquivoDestino.length() > 0) {
                println "Download concluído: ${arquivoDestino.name}"
                return arquivoDestino
            } else {
                println "Falha ao salvar"
                return null
            }
            
        } catch(Exception e) {
            println "Erro ao baixar: ${e.message}"
            return null
        }

    }

    private String acessarEspacoPrestadorServicoSaude(String homePage){

        Document doc = Jsoup.parse(homePage)
        if(!homePage) return null


        Element primeiroLink = doc.selectFirst('a:containsOwn(Prestador de Servi)')
        if(!primeiroLink) return "Erro ao encontrar o primeiro link"

        String primeiroHref = processarUrl(primeiroLink.attr('href'))
        def primeiraPagina = receberPagina(primeiroHref)
        if(!primeiraPagina) return "Erro ao acessar primeira página" else {println "Link [1] - OK" }

        return primeiraPagina

    }

    private acessarTISSPadraTrocaInfoSaudeSuple(String primeiraPagina){

        if(!primeiraPagina) return null

        Document docSegunda = Jsoup.parse(primeiraPagina)
        Element segundoLink = docSegunda.selectFirst('a:containsOwn(TISS)')
        if(!segundoLink) return "Erro ao encontrar o segundo link"

        String segundoHref = processarUrl(segundoLink.attr('href'))
        def segundaPagina = receberPagina(segundoHref)
        if(!segundaPagina) return "Erro ao acessar segunda página" else {println "Link [2] - OK" }

        return segundaPagina
    }

    private acessarPadraoTISSAtualizado(String segundaPagina){


        if(!segundaPagina) return null

        Document docTerceira = Jsoup.parse(segundaPagina)
        Element terceiroLink = docTerceira.selectFirst('a:contains(Clique aqui para acessar a versão)')
        if(!terceiroLink) return "Erro ao encontrar o terceiro link"

        String terceiroHref = processarUrl(terceiroLink.attr('href'))
        def terceiraPagina = receberPagina(terceiroHref)
        if(!terceiraPagina) return "Erro ao acessar terceira página" else {println "Link [3] - OK" }

        return terceiraPagina

    }

    private acessarHistoricoPadraoTISS(String segundaPagina){

        if(!segundaPagina) return null

        Document docTerceira = Jsoup.parse(segundaPagina)
        Element terceiroLink = docTerceira.selectFirst('a:contains(Clique aqui para acessar todas as versões)')
        if(!terceiroLink) return "Erro ao encontrar o terceiro link"

        String terceiroHref = processarUrl(terceiroLink.attr('href'))
        def terceiraPagina = receberPagina(terceiroHref)
        if(!terceiraPagina) return "Erro ao acessar terceira página" else {println "Link [3] - OK" }

        return terceiraPagina

    }

    private acessarTabelaRelacionada(String segundaPagina){
        if(!segundaPagina) return null

        Document docTerceira = Jsoup.parse(segundaPagina)
        Element terceiroLink = docTerceira.selectFirst('a:contains(Clique aqui para acessar as planilhas)')
        if(!terceiroLink) return "Erro ao encontrar o terceiro link"

        String terceiroHref = processarUrl(terceiroLink.attr('href'))
        def terceiraPagina = receberPagina(terceiroHref)
        if(!terceiraPagina) return "Erro ao acessar terceira página" else {println "Link [3] - OK" }

        return terceiraPagina
    }

    private String baixarInformacoesHistorico(String url){
        try {
            String paginaHistorico = receberPagina(url)

            Document docHtml = Jsoup.parse(paginaHistorico)

            def csv = new File("Downloads","Historico_versoes.csv")

            def dataCorte = LocalDate.of(2016, 1, 1)
            def dataBR = DateTimeFormatter.ofPattern("MM/yyyy", new Locale("pt", "BR"))
            def dataBRCompleta = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"))


            csv.withWriter('UTF-8') { writer ->
                docHtml.select("tr").each { linha ->
                    def celulas = linha.select("td")

                    if (celulas.size() >= 3) {
                        String textoData = celulas[2].text().trim().toLowerCase()
                        LocalDate dataCelula = null

                        try {
                            dataCelula = LocalDate.parse(textoData, dataBRCompleta)
                        } catch (DateTimeParseException e1) {
                            try {
                                YearMonth ym = YearMonth.parse(textoData, dataBR)
                                dataCelula = ym.atDay(1)
                            } catch (DateTimeParseException e2) {
                            }
                        }

                        if (dataCelula != null && dataCelula.isAfter(dataCorte)) {

                            def dadosLinhaOriginal = celulas.collect { it.text().trim() }

                            def mes = dataCelula.getMonthValue()
                            def ano = dataCelula.getYear()

                            def novaLinhaParaCsv = dadosLinhaOriginal.take(2)
                            novaLinhaParaCsv.add(mes.toString())
                            novaLinhaParaCsv.add(ano.toString())
                            novaLinhaParaCsv.addAll(dadosLinhaOriginal.drop(3))

                            writer.writeLine(novaLinhaParaCsv.join(";"))
                        }
                    }
                }
            }



            return "Arquivo historico salvo!"
        }catch(Exception e){
            println "Erro ao baixar informações do histórico: ${e.message}"
            e.printStackTrace()
            return "Erro ao processar o histórico: ${e.message}"
        }
    }

    private taskBaixarArquivoTISSAtualizado(String homePage){
        try {

            if(!homePage) return "Erro ao acessar página inicial" else{ println "Link [Página Inicial] - OK" }

            def acessoPagina = acessarEspacoPrestadorServicoSaude(homePage)


            def acessoPaginaDois = acessarTISSPadraTrocaInfoSaudeSuple(acessoPagina)


            def acessoPadraoAtualizado = acessarPadraoTISSAtualizado(acessoPaginaDois)

            Document docQuarta = Jsoup.parse(acessoPadraoAtualizado)
            Element downloadLink = docQuarta.selectFirst('a:contains(Componente de Comunicação)')
            if(!downloadLink) return "Erro ao encontrar o link de download"

            String downloadHref = processarUrl(downloadLink.attr('href'))

            def arquivoBaixado = baixarArquivo(downloadHref, "Componente_de_Comunicacao.zip")

            if (arquivoBaixado) {
                return "Arquivo baixado"
            } else {
                return "Falha ao baixar"
            }
        } catch (Exception e) {
            return "Erro ao acessar: ${e.message}"
        }
    }

    private taskBaixarTabelaHistoricoVersoes(String homePage){
        try{
            if(!homePage) return "Erro ao acessar página inicial" else{ println "Link [Página Inicial] - OK" }

            def acessoPagina = acessarEspacoPrestadorServicoSaude(homePage)

            def acessoPaginaDois = acessarTISSPadraTrocaInfoSaudeSuple(acessoPagina)

            def acessarHistorico = acessarHistoricoPadraoTISS(acessoPaginaDois)


            baixarInformacoesHistorico(acessarHistorico)



        }catch(Exception e){
            println "Erro ao acessar: ${e.message}"
        }
    }

    private taskTabelasRelacionadas(String homePage){
        try {

            if(!homePage) return "Erro ao acessar página inicial" else{ println "Link [Página Inicial] - OK" }

            def acessoPagina = acessarEspacoPrestadorServicoSaude(homePage)


            def acessoPaginaDois = acessarTISSPadraTrocaInfoSaudeSuple(acessoPagina)


            def acessoTabelaRelacionada = acessarTabelaRelacionada(acessoPaginaDois)

            Document docQuarta = Jsoup.parse(acessoTabelaRelacionada)
            Element downloadLink = docQuarta.selectFirst('a:contains(Clique aqui para baixar a tabela de erros no envio para a ANS)')
            if(!downloadLink) return "Erro ao encontrar o link de download"

            String downloadHref = processarUrl(downloadLink.attr('href'))

            def arquivoBaixado = baixarArquivo(downloadHref, "Tabela_de_erros_no_envio_para_a_ANS.zip")

            if (arquivoBaixado) {
                return "Arquivo baixado"
            } else {
                return "Falha ao baixar"
            }
        } catch (Exception e) {
            return "Erro ao acessar: ${e.message}"
        }
    }
}