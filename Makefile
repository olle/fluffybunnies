.PHONY: start

##
## Default targets for starting
start:
	@docker-compose up -d
	@mvn clean verify
