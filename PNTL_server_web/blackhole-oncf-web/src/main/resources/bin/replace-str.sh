#!/bin/bash

if [ ${#} -ne 3 ]; then
    echo 'usage: sh replace-str.sh file_name pattern value' 
    exit 1
fi

file=${1}
pattern=${2}
value=${3}

sed -i "s/${pattern}/${value}/g" ${file}
