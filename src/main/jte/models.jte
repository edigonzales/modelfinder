@import java.util.List
<!--@import ch.so.agi.meta.Product-->

<!--@param List<Product> productList-->

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
    <script src="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/js/materialize.min.js"></script>

    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Material+Icons|Material+Icons+Outlined|Material+Icons+Two+Tone|Material+Icons+Round|Material+Icons+Sharp">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/css/materialize.min.css">
    <link rel="stylesheet" href="fonts.css">
    <link rel="stylesheet" href="modelfinder.css">

</head>

<body>
    <div class="container">
        <div style="margin-top:10px; text-align: right;">
            <img src="Logo.png" alt="Logo Kanton Solothurn">
        </div>

        <div style="margin-top:120px; margin-bottom:30px;">
            <div class="s12 m6 input-field">
                <div class="prefix"><i class="material-icons">search</i></div>
                <div class="suffix" hx-on:click="document.querySelector('#search-input').value = ''; htmx.trigger('#search-input', 'input');"><i class="material-icons">clear</i></div>
                <input id="search-input" placeholder=" " type="text" autocomplete="off"
                name="search" 
                hx-post="/search"
                hx-trigger="input changed delay:500ms, keyup[key=='Enter'], load"
                hx-target="#search-results">
                <label for="search-input">Search for INTERLIS models...</label>    
            </div>
        </div>



        <div class="left-align" style="margin-top:30px; font-size: 16px; line-height: 1.42857143;">
            Lorem ipsum odor amet, consectetuer adipiscing elit. Nisi donec vulputate posuere auctor dictum ante. Eu convallis mi consequat per sollicitudin vestibulum odio ut maximus. Maecenas tellus sodales eget nunc volutpat nam integer? Efficitur lobortis posuere aptent id aliquam nec nibh litora. Accumsan enim feugiat morbi, integer pharetra ad. Nec aenean rhoncus ridiculus arcu sapien est vitae. Natoque nisl arcu habitasse morbi netus egestas justo. Rhoncus tellus pellentesque vivamus purus phasellus varius suscipit.
            <br/><br/>Ad diam tristique netus conubia dui maecenas pretium aenean. Ridiculus dictum ligula pretium, ridiculus maecenas tortor. Velit pulvinar lectus hac pretium inceptos duis? Tristique facilisi neque odio; dapibus praesent lobortis. Proin ex gravida magnis scelerisque tristique venenatis. Ligula ex facilisis montes vestibulum vel etiam non?	        
        </div>

        <!--

        <div style="margin-top:40px; margin-bottom:30px;">
            <div class="s12 m6 input-field">
                <div class="prefix"><i class="material-icons">search</i></div>
                <div class="suffix" hx-on:click="document.querySelector('#search-input').value = ''; htmx.trigger('#search-input', 'input');"><i class="material-icons">clear</i></div>
                <input id="search-input" placeholder=" " type="text"
                name="search" 
                hx-post="/search"
                hx-trigger="input changed delay:500ms, keyup[key=='Enter'], load"
                hx-target="#search-results">
                <label for="search-input">Suchbegriff</label>    
            </div>
        </div>

        <div>${productList.size()}</div>
        -->
        <!--
        <table class="responsive-table">
            <colgroup>
                <col style="width: 2%" />
                <col style="width: 2%" />
                <col style="width: 22%" />
                <col style="width: 58%" />
                <col style="width: 8%" />
                <col style="width: 8%" />
            </colgroup>

            <thead>
            <tr>
                <th style="padding-right:0px;"></th>
                <th colspan="2">Titel</th>
                <th>Beschreibung</th>
                <th>Karte</th>
                <th>Details</th>
            </tr>
            </thead>
            <tbody id="search-results">
                @template.search-results(productList = productList)
            </tbody>
        </table> 
        -->
           
    </div>
</body>
</html>
