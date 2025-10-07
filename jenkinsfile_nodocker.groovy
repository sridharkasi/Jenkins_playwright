pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Start demo app') {
      steps {
    
        sh '''
          nohup python3 -m http.server 5000 --directory site --bind 0.0.0.0 > app.log 2>&1 &
          echo $! > app.pid
          # wait for server to boot (simple check)
          for i in 1 2 3 4 5; do
            curl -sS http://127.0.0.1:5000 && break || sleep 1
          done
        '''
      }
    }
	
	
    stage('Setup Python') {
        steps {
                sh '''
                  python3 -m venv venv
                  source venv/bin/activate
                  pip install --upgrade pip
                  pip install -r requirements.txt
				  # Install Playwright browsers (Chromium with dependencies)
                  playwright install chromium --with-deps
                '''
            }
	}
        
		
		
	
    stage('Run Playwright tests') {
      steps {
       
         sh '''
         	test_playwright.py
            '''
      }
    }

    stage('Teardown') {
      steps {
        sh '''
          if [ -f app.pid ]; then
            kill $(cat app.pid) || true
            rm -f app.pid
          fi
        '''
      }
    }
  } // stages
  post {
    always {
      archiveArtifacts artifacts: 'app.log', allowEmptyArchive: true
    }
  }
}
