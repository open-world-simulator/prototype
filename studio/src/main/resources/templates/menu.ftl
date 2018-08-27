<#if context.experiments??>
<div class="form-group">
    <label for="experiment">Select experiment:</label>
    <select class="form-control" id="experiment" name="experiment" required
        onchange="document.location='/configuration?id=' + this.value;">
        <option value="">-- NEW EXPERIMENT --</option>
        <#list context.experiments as experiment>
        <option <#if context.experiment.experimentId?? && context.experiment.experimentId == experiment.experimentId> selected</#if> value="${experiment.experimentId}">${experiment.experimentId}</option>
        </#list>
    </select>
</div>
</#if>

<ul class="nav nav-pills nav-stacked">
    <li role="presentation" <#if context.template?contains("configuration")> class="active"</#if>><a href="/configuration<#if context.experiment.experimentId??>?id=${context.experiment.experimentId}</#if>">Configuration</a></li>
<#if context.experiment.experimentId??>
    <li role="presentation" <#if context.template?contains("parameters")> class="active"</#if>><a href="/parameters?id=${context.experiment.experimentId}">Parameters</a></li>
    <li role="presentation" <#if context.template?contains("status")>     class="active"</#if>><a href="/status?id=${context.experiment.experimentId}">Status</a></li>
    <li role="presentation" <#if context.template?contains("results")>    class="active"</#if>><a href="/results?id=${context.experiment.experimentId}">Results</a></li>
</#if>
</ul>
