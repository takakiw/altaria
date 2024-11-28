#!/bin/bash

NACOS_HOST=nacos
NACOS_PORT=8848
CONFIG_FILE="nacos-config.zip"
POLICY="OVERWRITE"

if [ -n "$1" ]; then
    CONFIG_FILE="$1"
fi

LOGIN_RESPONSE=$(curl -s -X POST "http://$NACOS_HOST:$NACOS_PORT/nacos/v1/auth/login" -d 'username=nacos&password=nacos')
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | awk -F'"' '{print $4}')

if [ -z "$ACCESS_TOKEN" ]; then
    echo "登录失败，无法获取 accessToken"
fi

UPLOAD_RESPONSE=$(curl -s --location \
    --request POST "http://$NACOS_HOST:$NACOS_PORT/nacos/v1/cs/configs?import=true" \
    --header "Authorization: Bearer $ACCESS_TOKEN" \
    --form "policy=$POLICY" \
    --form "file=@$CONFIG_FILE")

echo "上传配置结果: $UPLOAD_RESPONSE"
