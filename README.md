# Software angels – alramalho

### Run
Go to the root of the project and run
```
docker-compose up
```

The frontend app will be accessible through `localhost:8080` and the backend app through `localhost:7000`


### Design decisions

- Database as `JSON`
    - Data is very simple and fixed, and already comes on a `JSON` format. Using relational databases would be adding unnecessary complexity, at least since more complicated data manipulation requirements would come. Backend is structured in a way that changing the database layer is as effortless as possible
- Exposed API Key
    - I understood that security was not part of the evaluation criteria. 

### Possible optimizations

- Use yarn instead of npm in docker compose, faster boot times