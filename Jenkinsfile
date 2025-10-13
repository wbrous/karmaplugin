pipeline {
  agent {
    dockerContainer {
      image 'maven:3.9.6-eclipse-temurin-22'
    }

  }
  stages {
    stage('Checkout') {
      steps {
        echo 'Checking out source code...'
        checkout scm
      }
    }

    stage('Build') {
      steps {
        echo 'Building KarmaPlugin with Maven...'
        sh 'mvn -B -DskipTests clean package'
      }
    }

    stage('Test') {
      steps {
        echo 'Running tests...'
        echo 'No tests configured yet'
      }
    }

    stage('Archive Artifacts') {
      steps {
        echo 'Archiving build artifacts...'
        archiveArtifacts(artifacts: 'target/*.jar', fingerprint: true)
      }
    }

  }
  post {
    success {
      echo 'Build succeeded! ✓'
      echo 'Artifact: KarmaPlugin-0.1.2-BETA.jar'
    }

    failure {
      echo 'Build failed! ✗'
    }

    always {
      echo 'Cleaning up workspace...'
      cleanWs()
    }

  }
}