<#if context.experiments??>
<div class="form-group">
    <label for="experiment">Select experiment:</label>
    <select class="form-control" id="experiment" name="experiment" required
        onchange="document.location='/configure?id=' + this.value;"
    >
        <option value="">-- NEW EXPERIMENT --</option>
        <#list context.experiments as experiment>
        <option <#if context.experiment.experimentId?? && context.experiment.experimentId == experiment.experimentId> selected</#if> value="${experiment.experimentId}">${experiment.experimentId}</option>
        </#list>
    </select>
</div>
</#if>

<ul class="nav nav-pills nav-stacked">
    <li role="presentation" <#if context.template?contains("configure")> class="active"</#if>><a href="/configure">Configuration</a></li>
<#if context.experiment.experimentId??>
    <li role="presentation" <#if context.template?contains("status")>    class="active"</#if>><a href="/status?id=${context.experiment.experimentId}">Status</a></li>
    <li role="presentation" <#if context.template?contains("results")>   class="active"</#if>><a href="/results?id=${context.experiment.experimentId}">Results</a></li>
</#if>
</ul>
