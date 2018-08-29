<!--------------------------------------------------------------------------------------------------------------------->
<!DOCTYPE html>
<html lang="en">
<head><#include "head.ftl"></head>
<body>
<#include "top.ftl">

<div class="container-fluid">
    <div class="row">
        <div class="col-md-2">
            <#include "menu.ftl">
        </div>
        <div class="col-md-10">
            <div class="row">
                <legend>Results for Experiment <strong>${context.experiment.experimentId}</strong>:</legend>
            </div>
            <div class="row">
            <#if context.results??>
            <ul class="nav nav-pills nav-stacked">
            <#list context.results as result>
            <li role="presentation"><a target="_blank" href="/static/${context.experiment.experimentId}${result}">${result}</a></li>
            </#list>
            </ul>
            </#if>
            </div>
        </div>
    </div>
</div>
</body>
</html>


