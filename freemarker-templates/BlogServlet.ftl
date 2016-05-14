<#import "common.ftl" as u>
<@u.template>
<main>
    <h2>Posts</h2>
    <#if posts?has_content>
        <#list posts as post>
            <section>
                <header>
                    <h3><a href="/${post["date"]?string["yyyy/MM"]}/${post["name"]}">${post["title"]}</a></h3>
                    <time>${post["date"]?date}</time>
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

        <#if page??>
            <div><a href="/page/${page + 1}">More posts</a></div>
        </#if>
    </#if>
</main>
</@u.template>