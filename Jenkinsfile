pipeline {
    agent any

    triggers {
        // Requirement: Perodically check for updates from Git (Poll SCM every 5 minutes)
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
                echo 'Starting automated Maven compilation and test suite...'
                // Grants execution permission to the Maven wrapper script
                sh 'chmod +x mvnw'
                // Compiles, builds, and runs tests automatically
                sh './mvnw clean package'
            }
        }

        stage('Ansible Deployment') {
            steps {
                echo 'Build & Test passed successfully! Deploying to Web Server...'
                // Executes the Ansible playbook to deploy the application
                sh 'ansible-playbook -i ansible/inventory.ini ansible/deploy.yml'
            }
        }
    }

    post {
        failure {
            echo 'Build failed. Orchestrating notification emails...'
            // Requirement: send cc email to srengty@gmail.com and the developer who committed the error
            emailext (
                to: 'srengty@gmail.com',
                subject: "ALERT: Jenkins Build Failure - ${env.JOB_NAME} [Build #${env.BUILD_NUMBER}]",
                body: """Hello Team,

The latest build pipeline has encountered an execution error.

Job Name: ${env.JOB_NAME}
Build Number: #${env.BUILD_NUMBER}
Console Logs: ${env.BUILD_URL}console

Please inspect the log output to trace and resolve the issue.""",
                recipientProviders: [
                    [$class: 'CulpritsRecipientProvider'],   // Sends to the developer who committed the error
                    [$class: 'DevelopersRecipientProvider']  // Sends to the commit author
                ]
            )
        }
    }
}