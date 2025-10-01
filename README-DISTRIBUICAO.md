# IBM Cloud Object Storage Client - Guia de Distribuição

## 📦 Pacote para Distribuição

Sua aplicação já está configurada para gerar um JAR executável que pode ser distribuído facilmente.

### 🚀 Como Gerar o JAR Executável

```bash
mvn clean package
```

Isso criará o arquivo `target/ibm-cos-client.jar` (~8.8MB) que contém:
- ✅ Sua aplicação
- ✅ Todas as dependências do IBM COS SDK
- ✅ Look and feel nativo do sistema operacional
- ✅ Configurações otimizadas de UI

### 📁 Estrutura para Distribuição

Para distribuir a aplicação, crie uma pasta com:

```
ibm-cos-client/
├── ibm-cos-client.jar     # JAR executável principal
├── run.sh                 # Script para macOS/Linux
├── run.bat               # Script para Windows
└── README-DISTRIBUICAO.md # Este arquivo
```

### 🖥️ Execução

#### Windows
- Duplo clique em `run.bat` ou execute:
```cmd
java -jar ibm-cos-client.jar
```

#### macOS/Linux
- Execute `./run.sh` ou:
```bash
java -jar ibm-cos-client.jar
```

### ⚙️ Requisitos do Sistema

- **Java 8 ou superior** (recomendado Java 11+)
- **Memória**: Mínimo 512MB RAM
- **Sistema Operacional**: Windows, macOS ou Linux
- **Resolução**: Mínimo 900x700 pixels

### 🎨 Look and Feel

A aplicação automaticamente detecta e usa o look and feel nativo:
- **Windows**: Windows Look and Feel
- **macOS**: Aqua Look and Feel (com menu na barra superior)
- **Linux**: GTK+ Look and Feel

### 🔧 Configurações Avançadas

Para usuários avançados, você pode executar com opções personalizadas:

```bash
# Aumentar memória
java -Xmx1g -jar ibm-cos-client.jar

# Forçar look and feel específico
java -Dswing.defaultlaf=javax.swing.plaf.nimbus.NimbusLookAndFeel -jar ibm-cos-client.jar

# Debug de conexão
java -Dcom.ibm.cos.client.debug=true -jar ibm-cos-client.jar
```

### 📝 Funcionalidades

- ✅ Conectar com IBM Cloud Object Storage
- ✅ Navegar por buckets e objetos
- ✅ Upload de arquivos
- ✅ Download de arquivos
- ✅ Exclusão de objetos
- ✅ Interface nativa do sistema operacional
- ✅ Suporte a pastas/prefixos

### 🆘 Solução de Problemas

**Aplicação não inicia:**
- Verifique se Java está instalado: `java -version`
- Certifique-se que o JAR não está corrompido
- Tente executar pelo terminal para ver mensagens de erro

**Interface estranha:**
- A aplicação usa automaticamente o look and feel do sistema
- No macOS, o menu aparece na barra superior (comportamento nativo)

**Problemas de conexão:**
- Verifique suas credenciais IBM Cloud
- Confirme o endpoint correto
- Teste conectividade de rede

### 📞 Suporte

Este é um cliente desenvolvido seguindo princípios SOLID para facilitar manutenção e extensão. 
O código fonte está organizado em camadas bem definidas para fácil manutenção.

---
*Gerado automaticamente em ${maven.build.timestamp}*