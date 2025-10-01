IBM Cloud Object Storage Desktop Client
======================================

Cliente Java com interface Swing para gerenciar arquivos no IBM Cloud Object Storage.

Pré-requisitos:
- Java JDK 8 ou superior
- Maven 3.x

Compilação e Execução:

1. Compilar o projeto:
   mvn clean package

2. Executar a aplicação:
   java -jar target/ibm-cos-client-1.0-SNAPSHOT.jar

   Ou usando Maven:
   mvn exec:java -Dexec.mainClass="com.ibm.cos.client.IBMCloudCOSClient"

Como Usar:

1. Obter Credenciais:
   - Acesse o IBM Cloud Console
   - Navegue até seu serviço Cloud Object Storage
   - Vá em Service Credentials
   - Crie ou visualize uma credencial HMAC
   - Copie o "access_key_id" e "secret_access_key"

2. Configurar Endpoint:
   O endpoint padrão é: s3.us-south.cloud-object-storage.appdomain.cloud

   Endpoints por região:
   - us-south: s3.us-south.cloud-object-storage.appdomain.cloud
   - us-east: s3.us-east.cloud-object-storage.appdomain.cloud
   - eu-gb: s3.eu-gb.cloud-object-storage.appdomain.cloud
   - eu-de: s3.eu-de.cloud-object-storage.appdomain.cloud
   - jp-tok: s3.jp-tok.cloud-object-storage.appdomain.cloud

3. Conectar:
   - Insira Access Key ID
   - Insira Secret Access Key
   - Configure o Endpoint correto
   - Clique em "Connect"

4. Gerenciar Arquivos:
   - Selecione um bucket no dropdown
   - Navegue por pastas com duplo clique
   - Use ".. (parent)" para voltar ao diretório anterior
   - Upload: selecione arquivo local para enviar
   - Download: selecione arquivo e escolha destino local
   - Delete: selecione objeto e confirme exclusão

Funcionalidades:
- Autenticação usando HMAC credentials
- Listagem de buckets disponíveis
- Navegação por estrutura de diretórios/prefixes
- Upload de arquivos
- Download de arquivos
- Exclusão de objetos
- Visualização de tamanho e data de modificação
- Interface responsiva com feedback de operações

Estrutura do Projeto:
- src/com/ibm/cos/client/IBMCloudCOSClient.java: Classe principal
- pom.xml: Configuração Maven com dependências do IBM COS SDK

Dependências:
- IBM Cloud Object Storage SDK: 2.13.4
- Java Swing (incluído no JDK)

Solução de Problemas:

- Erro de autenticação: Verifique se as credenciais estão corretas
- Erro de conexão: Confirme que o endpoint está correto para sua região
- Timeout: Aumente o timeout no código se necessário (linha 198)
- Buckets não aparecem: Verifique as permissões da credencial HMAC
