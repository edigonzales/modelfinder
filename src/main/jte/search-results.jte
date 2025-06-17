@import java.util.List
@import java.time.LocalDate
@import java.lang.String
@import ch.so.agi.meta.Product

@param List<Product> productList
@param String baseUrl = "https://sos-ch-dk-2.exo.io/ch.so.agi.fubar1/"


<!-- TODO: nicht alle haben einen WGC-Link, solche, die nur als WMS oder dataservice verfügbar sind -->


@for (var product : productList) 
    <tr>
        <td  style="padding-right:0px;">
            @if(product.dtype().equals("simiProduct_LayerGroup"))
                <i style="cursor: pointer;" class="material-icons" hx-on:click="this.closest('tbody').querySelectorAll('.child-row-${product.id()}').forEach(el => el.classList.toggle('expanded'))">add</i>
            <!--@elseif(product.dtype().equals("simiProduct_ExternalWmsLayers"))
                <i class="material-icons-two-tone">open_in_new</i>-->
            @else 
                <!--<i class="material-icons-two-tone">map</i>-->
                <i class="material-icons-outlined">map</i>
            @endif
        </td>
        <td colspan="2">${product.title()}</td>
        <!--<td>${product.dtype()}</td>-->
        <td>
            @if(product.description() != null) 
                $unsafe{product.description()}
            @elseif(product.description_override() != null) 
                $unsafe{product.description_override()}
            @elseif(product.description_model() != null) 
                $unsafe{product.description_model()}            
            @endif
        </td>
        <td>
            @if(product.transparency() != null && !product.transparency().equals("0"))
                <a href="https://geo.so.ch/map?l=${product.ident_part()}[${product.transparency()}]" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a>
            @else
                <a href="https://geo.so.ch/map?l=${product.ident_part()}" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a>                
            @endif
        </td>
        <td><a href="product/${product.ident_part()}" target="_blank"><i class="material-icons-outlined black-link">info</i></a></td>
    </tr>

    @if(product.children().length > 0)

        @for (var child : product.children())
        <tr class="child-row-${product.id()} hidden">
            <td></td>
            <td  style="padding-right:0px;"><i class="material-icons-outlined">map</i></td>
            <td>${child.title()}</td>
            <td>
                @if(child.description() != null) 
                    $unsafe{child.description()}
                @elseif(child.description_override() != null) 
                    $unsafe{child.description_override()}
                @elseif(child.description_model() != null) 
                    $unsafe{child.description_model()}            
                @endif
            </td>
            <td>
                @if(child.transparency() != null && !child.transparency().equals("0"))
                    <a href="https://geo.so.ch/map?l=${child.ident_part()}[${child.transparency()}]" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a>
                @else
                    <a href="https://geo.so.ch/map?l=${child.ident_part()}" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a>                
                @endif            
            </td>
            <td><a href="product/${child.ident_part()}" target="_blank"><i class="material-icons-outlined black-link">info</i></a></td>
        </tr>
        @endfor
    @endif
@endfor
    