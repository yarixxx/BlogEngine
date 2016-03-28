<#import "common.ftl" as u>
<@u.template>

<div>
<#if post??>
    <div>
        <div>
            <h2>${ post["title"] }</h2>
            <span>${ post["date"]?date }</span>
        </div>

        <div>
            ${ post["content"] }
        </div>
    </div>

        <#if post["tags"]??>
            <div>
                Tags:
                <#list post["tags"] as tag>
                    <span>${tag}</span>
                </#list>
            </div>
        </#if>
    <#else>
    <div>
        <p>Post Not found.</p>
    </div>
    </#if>
</div>

</@u.template>