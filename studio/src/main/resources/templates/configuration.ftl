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
            <form method="GET" action="/save-configuration">
                <div class="row">
                    <#include "success.ftl">

                <legend>Experiment configuration:</legend>
                    <div class="form-group">
                        <label for="id">Experiment Id</label>
                        <input type="text" class="form-control" id="id" name="id"
                               value="${context.experiment.experimentId!}"
                               required
                               placeholder="Unique identifier for this experiment - only letters, numbers and '-' allowed">
                    </div>

                    <div class="form-group">
                        <label for="baseConfiguration">Base configuration</label>
                        <select class="form-control" id="baseConfiguration" name="baseConfiguration" required>
                            <#list context.configs as config>
                            <option <#if context.experiment.baseSimulationConfig?? && context.experiment.baseSimulationConfig==config>selected</#if>>${config}</option>
                            </#list>
                        </select>
                    </div>
                    <div class="form-group">
                        <div class="col-auto my-1">
                            <label for="nMonths">Number of months</label>
                            <input type="string" class="form-control" required id="nMonths"
                                   value="${context.experiment.months}"
                                   name="nMonths"
                                   required
                            >
                        </div>
                    </div>
                </div>

    <div class="row">
        <button type="submit" class="btn btn-primary">Save changes</button>
        </form>
    </div>
</div>
</div>
</div>

</body>
</html>


