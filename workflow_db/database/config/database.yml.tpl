login: &login
  username: ${DB_USERNAME}
  adapter: mysql2
  host: ${DB_HOSTNAME}
  port: ${DB_PORT}
  retries: 2
  password: ${DB_PASSWORD}
  encoding: utf8
  reconnect: true

workflow_docker_env:
  <<: *login
  database: workflow_docker_env

docker_env:
  <<: *login
  database: workflow_docker_env
