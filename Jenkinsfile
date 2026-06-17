pipeline {
    agent any

    triggers {
        // Periodically check for updates from Git every 5 minutes
        pollSCM('*/5 * * * *')
    }

    stages {
        stage('Git Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Auto Build & Test') {
            steps {
                echo 'Starting automated Maven compilation and test execution on Windows...'
                // Using 'bat' instead of 'sh' for native Windows execution
                bat 'mvnw.cmd clean package'
            }
        }

        stage('Ansible Deployment') {
            steps {
                echo 'Build & Test passed successfully!'
                echo 'Simulating target environment deployment via Ansible configurations...'
                echo 'Executing command: ansible-playbook -i ansible/inventory.ini ansible/deploy.yml'
                echo 'Deployment routing status: SUCCESSFUL'
            }
        }
    }

    post {
        failure {
            echo 'Pipeline execution failed. Orchestrating error notification emails...'
            emailext (
                to: 'srengty@gmail.com',
                subject: "ALERT: Jenkins Build Failure - ${env.JOB_NAME} [Build #${env.BUILD_NUMBER}]",
                body: """The latest deployment pipeline has encountered an execution error.

Job Name: ${env.JOB_NAME}
Build Number: #${env.BUILD_NUMBER}
Console Logs: ${env.BUILD_URL}console""",
                recipientProviders: [
                    [$class: 'CulpritsRecipientProvider'],
                    [$class: 'DevelopersRecipientProvider']
                ]
            )
        }
    }
}