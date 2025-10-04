class Main {
    static void main(String[] args) {
        def core = new Core()
        
        println 'Fazendo requisição para o SITE'
        def result = core.getResult()
        
        println 'Resposta do acesso:'
        println result
    }
}

