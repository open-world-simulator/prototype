<!--------------------------------------------------------------------------------------------------------------------->
<!DOCTYPE html>
<html lang="en">
<head><#include "head.ftl"></head>
<body>
<div class="container-fluid">
    <#include "top.ftl">
    <div class="row">
        <div class="col-md-2">
            <#include "menu.ftl">
        </div>
        <div class="col-md-10">

        </div>
        <div class="row">
            <#if !context.simulation.running>
            <h2>Simulation STOPPED</h2>
            <br>
            <a href="/execute?id=${context.experiment.experimentId}" class="btn btn-primary">Execute simulation</a>
            <#else>
            <h2>Simulation RUNNING:</h2>
            <p>
                Status: ${context.simulation.status!''}:
                <br>
                Month: ${context.simulation.currentMonth}</p>
            </#if>
    </div>
</div>
</body>
</html>


