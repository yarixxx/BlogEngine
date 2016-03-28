<#import "common.ftl" as u>
<@u.template>

<main>
    <h2>${ tag }</h2>
    <#if posts?has_content>
        <#list posts as post>
            <section>
                <header>
                    <h3><a href="/${post["date"]?string["yyyy/MM"]}/${post["name"]}">${post["title"]}</a></h3>
                    <span>${post["date"]?date}</span>
                </header>

                <div>
                    ${post["content"]}
                </div>

                <#if post["tags"]??>
                    <div>
                        <span>Tags:</span>
                        <#list post["tags"] as tag>
                            <span>${tag}</span>
                        </#list>
                    </div>
                </#if>
            </section>
        </#list>
    </#if>
</main>

</@u.template>