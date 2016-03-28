<#import "simple.ftl" as u>
<@u.template>
    <h2>Tags:</h2>
    <div class="y3x-tags-map">
        <#list allTags as tag>
            <a href="/tag/${tag.tagCode}" class="y3x-tag">${tag.tagName} (${tag.count})</a>
        </#list>
    </div>
</@u.template>