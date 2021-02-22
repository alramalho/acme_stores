# Software angels – Alexandre Ramalho
Challenge of software angels, attemped by [alramalho](https://alramalho.com)*

<sup><sub><sub>*já agora faço publicidade</sub></sub><sup>

### Running
Go to the root of the project and run
```
docker-compose up
```

The frontend app will be accessible through `localhost:8080` and the backend app through `localhost:7000`


### Design decisions
- No "hard" documentation
  - Tests are documentation, documentation is one more thing that
    can get outdated and needs active effort to be maitained. If testes are
    rightly written in a behaviour driven way, they will serve as documentation,
    giving live working examples of the code, while still explaining them.
- Relational database as PostgreSQL
    - I kinda of feel this is an overkill, and thought about going with some simpler solutions,
      such as JSON. Although, since I know it is used in the project I'm applying for, and since I
      wanted to get more experienced with it, I've decided to go for it
- Exposed API Key, database user and password
    - I understood that security was not part of the evaluation criteria. 

### Possible optimizations

- Use yarn instead of npm in docker compose, faster boot times
- Silence warnings when running backend