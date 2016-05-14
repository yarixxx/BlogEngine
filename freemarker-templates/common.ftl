<#macro template>
<html>
<head>
    <title>${ title }</title>
    <link href="/static/styles.css" type='text/css' rel='stylesheet' />
    <link href="/static/favicon.ico" type="image/x-icon" rel="shortcut icon" />
</head>
<body>

<header>
    <h1>${ title }</h1>
</header>

<div class="body">
    <aside>
        <section>
            <ul>
                <#list announce as post>
                    <li><a href="/${post["date"]?string["yyyy/MM"]}/${post["name"]}">${post.title}</a> ${post["date"]?date}</li>
                </#list>
            </ul>
        </section>
        <section>
            <ol>
                <#list topTags as tag>
                    <li><a href="/tag/${ tag.tagCode }">${ tag.tagName }</a> (${ tag.count })</li>
                </#list>
            </ol>
            <div><a href="/all-tags/">All tags</a></div>
        </section>
        <section>
            <ul>
                <#list archive as year>
                    <li><span>${ year.year?c }</span> <span>(${ year.count })</span></li>
                </#list>
            </ul>
        </section>
    </aside>

    <div class="content">
        <#nested>
    </div>
</div>

<footer>
    <div>${ copyrights!"no copy" }</div>
</footer>

</body>
</html>
</#macro>