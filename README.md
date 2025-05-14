# Vault4Tomcat

A lightweight **HashiCorp Vault** integration for **Apache Tomcat**.  
Securely fetch secrets at runtime, eliminate hardcoded credentials, and simplify Tomcat configuration.

![GitHub License](https://img.shields.io/github/license/dsoumis/Vault4Tomcat)
![Latest Release](https://img.shields.io/github/v/release/dsoumis/Vault4Tomcat)

## Introduction

**Vault4Tomcat** integrates **HashiCorp Vault** with **Apache Tomcat** to securely manage secrets in Tomcat configuration files.  

It dynamically resolves `${vault:path#key}` placeholders.

🔹 **Dynamic Secret Resolution** → Fetch secrets at runtime from HashiCorp Vault.  

**TODO:**  
🔹 **JNDI Integration**  
🔹 **Multi-Auth Support**  
🔹 **Secret Caching**  
🔹 **CLI Tool** 


**Security-First**: No more hardcoded passwords in configuration files.

---

## Installation

### Prerequisite: Have a Hasicorp Vault deployed
Example deployment:
```
podman run --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=my-root-token' -e 'VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200' -p 8200:8200 hashicorp/vault
```
in the container:
```
export VAULT_ADDR=http://127.0.0.1:8200
export VAULT_TOKEN=my-root-token
vault kv put secret/myapp/config username="admin" password="s3cr3t"
vault kv get secret/myapp/config 
```

### 1. Download the latest release:
[Vault4Tomcat Releases](https://github.com/dsoumis/Vault4Tomcat/releases)

### 2. Drop the JAR into Tomcat’s `lib/` directory:
```
cp vault4tomcat.jar $CATALINA_BASE/lib/
```
### 3. Define environment variables or create a `conf/vault.properties` file:
```
vault.address=<http://127.0.0.1:8200>
vault.token=<my-root-token>
vault.ssl.verify=true
```

### 4. Register the Property Source in `conf/catalina.properties`:
```
org.apache.tomcat.util.digester.PROPERTY_SOURCE=com.mycompany.tomcat.VaultPropertySource
```

### 5. Use Vault secrets in `.xml` files:
```
<Resource name="jdbc/MyDB" auth="Container" type="javax.sql.DataSource"
username="myuser"
password="${vault:database/creds#password}"
driverClassName="com.mysql.cj.jdbc.Driver"
url="jdbc:mysql://dbhost/mydb"/>
```

---

## How It Works
Vault4Tomcat intercepts `${vault:path#key}` placeholders in Tomcat config files and replaces them with secrets from HashiCorp Vault.

1. Tomcat starts up and loads configuration.  
2. VaultPropertySource detects placeholders like ${vault:...}.  
3. It queries Vault via VaultClient and retrieves secrets.  
4. Secrets are injected into Tomcat’s configuration at runtime.  
5. Secret caching reduces Vault API calls for performance.  

## Licensing
Vault4Tomcat is open-source and licensed under the Apache License 2.0.

## Contributing
To submit a GitHub Pull Request you'll need to fork the repository, clone your fork to do the work:

$ git clone https://github.com/$USERNAME/Vault4Tomcat.git

and then push your changes, and submit a Pull Request via the GitHub UI.