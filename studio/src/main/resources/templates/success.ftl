<#if context.success??>
    <#if context.success >
    <div class="alert alert-success" role="alert">Experiment configuration updated</div>
    </#if>
    <#if !context.success >
    <div class="alert alert-danger" role="alert">Error updating configuration. Please, correct the wrong fields.</div>
    </#if>
</#if>
