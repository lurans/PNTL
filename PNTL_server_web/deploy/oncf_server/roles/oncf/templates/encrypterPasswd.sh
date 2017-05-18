#!/bin/bash

encrypter="${chkflow_install_dir}/webapps/${chkflow_server}/WEB-INF/lib/oncf-encrypt-tool.jar"

encrypt_passwd()
{
	encrypted_passwd=$(su - admin -c "java -jar ${encrypter} $2 2> /dev/null")
    echo -e "${key}=\"${encrypted_passwd}\""    
}

encrypt_passwd