pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile'
            args '-v $HOME/.m2:/root/.m2'
        }
    }
    
    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=/root/.m2/repository'
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
                sh 'mvn clean compile -B -V'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running tests...'
                // Add test command if you have tests configured
                // sh 'mvn test -B'
                echo 'No tests configured yet'
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging the plugin...'
                sh 'mvn package -B -DskipTests'
            }
        }
        
        stage('Archive') {
            steps {
                echo 'Archiving artifacts...'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
    
    post {
        success {
            echo 'Build succeeded! ✓'
            echo "Artifact: KarmaPlugin-0.1.2-BETA.jar"
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

