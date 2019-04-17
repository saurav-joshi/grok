node('Iaasimov-BuildNode')
{  
  stage('Init') {
      echo 'Initializing build'
      echo 'Checking out from SCM'
      checkout scm
  // end init stage
  }

  stage('Build') {
    sh 'mvn clean package'

    // zip the DB scripts
    zip archive: true, dir: 'src\\main\\resources\\db_scripts', glob: '', zipFile: 'target/dbscripts.zip'

    sh '''
      cp -r ${WORKSPACE}/src/main/resources/docker target/docker
      cp -r ${WORKSPACE}/src/main/resources/db_scripts target/docker/iaasimov-db
      cp -r target/*.jar target/docker/iaasimov-grok
      cp -r ${WORKSPACE}/keystore.p12 target/docker/iaasimov-grok
    '''
    zip archive: true, dir: 'target/docker', glob: '', zipFile: 'iaasimov-dockercompose.zip'

  } 

  stage('Export Container') {

  }
  // end provision

  stage('Test') {
    // integrity test. Start the browser, and use curl to try to hit the site.
    // Then, cleanup.
  }

  stage('Archive') {  
    archiveArtifacts artifacts: 'target/*.jar'
  }

  stage('Notify') {

  }
}

