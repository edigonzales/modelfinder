<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />

    <title>INTERLIS Model Finder • Kanton Solothurn</title>
    
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
    <!--<script src="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/js/materialize.min.js"></script>-->

    <!--<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Material+Icons|Material+Icons+Outlined|Material+Icons+Two+Tone|Material+Icons+Round|Material+Icons+Sharp">-->
    <!--<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/css/materialize.min.css">-->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <link rel="stylesheet" href="fonts.css">
    <link rel="stylesheet" href="modelfinder.css">

</head>

<body>
    <div class="container">
        <div style="margin-top:10px; text-align: right;">
            <img src="Logo.png" alt="Logo Kanton Solothurn">
        </div>

        <div style="margin-top:120px; margin-bottom:40px;">
            <div class="input-container">
                <span class="magnifier-icon"><i class="bi bi-search" style="color: #333; font-size:1.2rem;"></i></span> 
                <input id="search-input" class="search-input"
                    type="text" 
                    name="query" 
                    hx-get="models" 
                    hx-target="#results" 
                    hx-trigger="input keyup changed delay:300ms"
                    hx-headers='{"Accept": "text/html"}' 
                    autocomplete="off" 
                    placeholder="Search for INTERLIS models..." />
                    <span class="clear-icon" hx-on:click="document.querySelector('#search-input').value = ''; htmx.trigger('#search-input', 'input');"><i class="bi bi-x" style="color: #333; font-size:1.2rem;"></i></span>
            </div>     
        </div>

        <div id="results"></div>

    </div>
</body>
</html>
