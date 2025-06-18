@import java.util.List
@import java.time.LocalDate
@import java.lang.String
@import ch.so.agi.modelfinder.ModelSearchResult

@param List<ModelSearchResult> modelSearchResults

@for (var searchResult : modelSearchResults) 
    <details class="repo-details">
        <summary class="repo-summary">
            ${searchResult.serverDisplayName()} (${searchResult.modelCount()})
        </summary>
        
        <table>
            <colgroup>
                <col style="width: 2%" />
                <col />
                <col style="width: 12%"/>
                <col style="width: 12%"/>
                <col style="width: 12%" />
                <col style="width: 12%" />
            </colgroup>
            <thead>
            <tr>
                <th>&nbsp;</th>
                <th>Name</th>
                <th>Version</th>
                <th>ILI-Version</th>
                <th>Detail</th>
                <th>Datei</th>
            </tr>
            </thead>
            <tbody>
            @for (var metadata : searchResult.models()) 
                <tr>
                    <td><i class="bi bi-file-earmark-text" style="font-size: 1.2rem;"></i></td>
                    <td>${metadata.name()}</td>
                    <td>${metadata.version()}</td>
                    <td>${metadata.schemaLanguage().substring(3).replace("_", ".")}</td>
                    <td>
                        <a href="models?serverUrl=${metadata.serverUrl()}&file=${metadata.file()}" target="_blank">
                            <i class="bi bi-info-circle" style="font-size: 1.2rem;"></i>                        
                        </a>
                    </td>
                    <td>
                        <a href="${metadata.serverUrl()}/${metadata.file()}" target="_blank">
                            <i class="bi bi-box-arrow-up-right" style="font-size: 1.2rem;"></i>                        
                        </a>
                    </td>
                </tr>
            @endfor
            </tbody>
        </table>
    </details>
@endfor    