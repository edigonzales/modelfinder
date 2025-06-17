# modelfinder

```
-Dspring.profiles.active=prod 
--spring.profiles.active=prod
```


```
curl -H "Accept: application/json" "http://localhost:8080/models?query=09"
curl -H "Accept: text/html" "http://localhost:8080/models?query=09"
```

```
.input-field input[type=text] {
    /*background-color: rgb(245, 245, 245);*/
    background-color: #F4F4F4;
}

/* label focus color */
.input-field input[type=text]:focus + label {
    color: rgb(198, 40, 40);
}

/* label underline focus color */
.input-field input[type=text]:focus {
    border-bottom: 1px solid rgb(198, 40, 40);
    /*box-shadow: 0 1px 0 0 #333333;*/
}
/* valid color */
.input-field input[type=text].valid {
    border-bottom: 1px solid #333333;
    box-shadow: 0 1px 0 0 #333333;
}
```