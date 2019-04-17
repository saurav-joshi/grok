#!/bin/sh

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

wget https://bootstrap.cn.oracle.com/jenkins/job/Iaasimov/lastSuccessfulBuild/artifact/target/iaasimov-0.5.1-SNAPSHOT.jar

# Variables for the server
CLOUD_USER='cloud.admin'
CLOUD_ID_DOMAIN='gse00003349'
CLOUD_ENDPOINT='https://api-z12.compute.em2.oraclecloud.com'
CLOUD_PW='claSsIc@1Gold'
DEMO_NAME='OARDC-Iaasimov'

#Simple check.
if [ -z "$CLOUD_USER" ] ; then
  echo "Script is not configured yet. Please fill in the details of the OPC cloud instance"
  exit 1
fi

PRIVATE_KEY=$CURRENT_DIR/$DEMO_NAME-key
PUBLIC_KEY=$PRIVATE_KEY.pub
echo "Managing keys"
echo "... Cleaning up existing keys"
rm -f $PRIVATE_KEY
rm -rf $PUBLIC_KEY

echo "... Generating new keys"
# generate a set of keys
echo -e "y\n" | /usr/bin/ssh-keygen -t rsa -b 2048 -f $PRIVATE_KEY -q -N ''
ERR_CODE=${PIPESTATUS[1]}
if [ $ERR_CODE -ne "0" ] ; then
  exit $ERR_CODE
fi


echo "Generating Terraform Parameter files"
echo "... Cleaning up (any) existing parameter files"
TARGET_FILE=$CURRENT_DIR/$DEMO_NAME.tfvar

echo "... Generating terraform parameters"
echo "user=\"${CLOUD_USER}\"" > $TARGET_FILE
echo "domain=\"${CLOUD_ID_DOMAIN}\"" >> $TARGET_FILE
echo "endpoint=\"${CLOUD_ENDPOINT}\"" >> $TARGET_FILE
echo "password=\"${CLOUD_PW}\"" >> $TARGET_FILE
echo "demo_name=\"${DEMO_NAME}\"" >> $TARGET_FILE
echo "owner=\"dchia\"" >> $TARGET_FILE
echo "ssh_public_key_file=\"$PUBLIC_KEY\"" >> $TARGET_FILE
echo "ssh_private_key_file=\"$PRIVATE_KEY\"" >> $TARGET_FILE

#terraform plan -var-file="$TARGET_FILE"
#terraform apply -var-file="$TARGET_FILE" -state="$CURRENT_DIR/terraform.tfstate"
