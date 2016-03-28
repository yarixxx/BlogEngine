<#macro template>
<html>
<head>
    <title>${ title }</title>
    <link href="/static/style.css" type='text/css' rel='stylesheet' />
</head>
<body>

<header>
    <h1>${ title }</h1>
</header>

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

<#nested>

<footer>
    <div>${ copyrights }</div>
</footer>

</body>
</html>
</#macro>