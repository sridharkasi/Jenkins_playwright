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
        // start a simple static server in background and record pid
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

    stage('Run Playwright tests (in Docker)') {
      steps {
        // Use a Playwright python image to run tests. Replace version if needed.
        sh '''
          docker run --rm --network=host \
            -v "$WORKSPACE":/workspace -w /workspace \
            mcr.microsoft.com/playwright/python:v1.54.0-noble \
            bash -lc "python3 -m pip install -r requirements.txt && playwright install chromium --with-deps && pytest -q"
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
