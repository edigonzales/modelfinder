@import ch.so.agi.modelfinder.ModelMetadata

@param ModelMetadata metadata

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />

    <title>${metadata.name()} • INTERLIS Model Finder • Kanton Solothurn</title>
    
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
    <script src="https://cdn.jsdelivr.net/npm/prismjs/prism.js"></script>
    <script src="prism-interlis.js"></script>
    
    <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
    <script>
        mermaid.initialize({ startOnLoad: false });

        // This function is triggered after HTMX loads the diagram
        function renderMermaid() {
            mermaid.init(undefined, "#diagram-container");
        }
    </script>
    
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/prismjs/themes/prism.css" />
    <link rel="stylesheet" href="fonts.css">
    <link rel="stylesheet" href="modelfinder.css">

</head>

<body>
    <div class="container">
        <div style="margin-top:10px; text-align: right;">
            <img src="Logo.png" alt="Logo Kanton Solothurn">
        </div>

        <div class="left-align" style="margin-top:60px;">
            <h1>${metadata.name()}</h1>            
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Short description</label>
            <div class="textedit-display textedit-display-line">
                ${metadata.shortDescription()}          
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Version</label>
            <div class="textedit-display textedit-display-line">
                ${metadata.version()}          
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Repository</label>
            <div class="textedit-display textedit-display-line">
                <a href="${metadata.serverUrl()}" target="_blank">${metadata.serverUrl()}</a>          
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">File</label>
            <div class="textedit-display textedit-display-line">
                <a href="${metadata.serverUrl()}/${metadata.file()}" target="_blank">${metadata.file()}</a>          
            </div>
        </div>
        
        <div class="textedit-container">
            <label class="textedit-label">Schema language</label>
            <div class="textedit-display textedit-display-line">
                ${metadata.schemaLanguage().substring(3).replace("_", ".")}          
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Issuer</label>
            <div class="textedit-display textedit-display-line">
                @if (metadata.technicalContact().matches("^(mailto|http).*")) 
                    <a href="${metadata.issuer()}" target="_blank">${metadata.issuer()}</a>
                @else
                    ${metadata.issuer()}
                @endif
            </div>
        </div>
        
        <div class="textedit-container">
            <label class="textedit-label">Technical contact</label>
            <div class="textedit-display textedit-display-line">
                @if (metadata.technicalContact().matches("^(mailto|http).*")) 
                    <a href="${metadata.technicalContact()}" target="_blank">${metadata.technicalContact()}</a>
                @else
                    ${metadata.technicalContact()}
                @endif
            </div>
        </div>        

        <div class="textedit-container">
            <label class="textedit-label">Further information</label>
            <div class="textedit-display textedit-display-line">
                @if (metadata.furtherInformation().matches("^(mailto|http).*")) 
                    <a href="${metadata.furtherInformation()}" target="_blank">${metadata.furtherInformation()}</a>
                @else
                    ${metadata.furtherInformation()}                          
                @endif
            </div>
        </div>
        
        <div class="textedit-container">
            <label class="textedit-label">MD5</label>
            <div class="textedit-display textedit-display-line">
                ${metadata.md5()}          
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Tags</label>
            <div class="textedit-display textedit-display-line">                
                @if(metadata.tags() != null && !metadata.tags().isBlank()) 
                    @for (var tag : metadata.tags().split(","))
                        <span class="badge">${tag}</span>          
                    @endfor
                @endif
            </div>
        </div>
        
        <div class="textedit-container">
            <label class="textedit-label">IDGeoIV</label>
            <div class="textedit-display textedit-display-line">                
                @if(metadata.idGeoIV() != null && !metadata.idGeoIV().isBlank()) 
                    @for (var idGeoIV : metadata.idGeoIV().split(","))
                        <span class="badge">${idGeoIV}</span>          
                    @endfor
                @endif
            </div>
        </div>
        
        <p>&nbsp;</p>

        <div class="textedit-container">
            <label class="textedit-label">Model</label>
            <div class="textedit-display textedit-display-area" style="overflow-y: auto;">
                <pre style="line-height: 1.52; font-size: 0.9rem;"><code class="language-interlis">${metadata.modelContent()}</code></pre>          
            </div>
        </div>

    </div>
</body>
</html>
