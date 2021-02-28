# ACME Corporation – [Alexandre Ramalho](https://alramalho.com)
Challenged by software angels:

<sup><sub><sub>*já agora faço publicidade</sub></sub><sup>

### Running
Go to the root of the project and run
```
docker-compose up
```

The frontend app will be accessible through `localhost:8080` and the backend app through `localhost:7000`


### Design decisions
- No "hard" documentation
  - Tests should be the only documentation, "hard" documentation is one more thing that
    can get outdated and needs active effort to be kept up to date. If testes are
    rightly written in a behaviour driven way, they will serve as documentation,
    giving live working examples of the code, while explaining them.
- Relational database as PostgreSQL
    - I kinda of feel this is an overkill, and thought about going with some simpler solutions. 
      Although, since I know it is used in the project I'm applying for, and since I
      wanted to get more experienced with it, I've decided to go for it
- Exposed API Key, database user and password
    - I understood that security was not part of the evaluation criteria. 
- All stores fields nullable except id and name. Was following the KISS and lean principle,
  since the id was the identifier it couldn't be null, and name was editable, I figured it made sense
  not to allow it to be null.
- Store `code` size of 1500
  - Nothing was specified in the API regarding special treatment on `code` parameter, therefore no spaces were trailed.
- CronJob as coroutine
  - I started by doing the cronjob as a separate application, running parallelly to the webapp. I feel like this also works,
  but coroutines and meant to be lightweight, and a routine on a global scope seemed like the leanest way to go.
### Possible optimizations

- Use yarn instead of npm in docker compose, faster boot times
- Silence warnings when running backend
- Use Enum for season half instead of varchar in DB
- Do not hardcode SQL code (to use FK of stores_seasons table). Related to [this issue](https://github.com/JetBrains/Exposed/issues/511)
- Use more specific exceptions throughout the code
- Only use one driver in repo tests
  - Decision made to decrease delivery time.
- Refactor CSV logic in the Handler, it doesn't read too well, and could be improved.
