$env:SPRING_PROFILES_ACTIVE='dev'
Set-Location .\backend
& "..\tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
