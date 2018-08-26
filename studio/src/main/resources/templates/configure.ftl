<!--------------------------------------------------------------------------------------------------------------------->
<!DOCTYPE html>
<html lang="en">
<head><#include "head.ftl"></head>
<body>
<div class="container-fluid">
    <div class="row">
        <ol class="breadcrumb">
            <li class="active">Open World Simulator [ABSURD PROTOTYPE EDITION]</li>
        </ol>
    </div>
    <div class="row">
        <div class="col-md-2">
            <#include "menu.ftl">
        </div>

        <script>
        function disableEmptyInputs(form) {
              var controls = form.elements;
              for (var i=0, iLen=controls.length; i<iLen; i++) {
                if( controls[i].classList.contains("sim-property") ) {
                    controls[i].disabled = controls[i].value == '';
                }
              }
        }

        </script>
        <div class="col-md-10">
            <form method="GET" action="/execute"
                  onsubmit="disableEmptyInputs(this);"
            >

                <div class="row">
                    <div class="alert alert-success" role="alert">Experiment configuration updated</div>
                    <div class="alert alert-danger" role="alert">Error updating experiment. Please, correct fields.
                    </div>

                    <legend class="scheduler-border">Experiment base parameters:</legend>
                    <div class="form-group">
                        <label for="experimentId">Experiment Id</label>
                        <input type="text" class="form-control" id="experimentId" name="experimentId"
                               value="${context.experiment.experimentId!}"
                               required
                               placeholder="Unique identifier for this experiment - only letters, numbers and '-' allowed">
                    </div>

                    <div class="form-group">
                        <label for="baseConfiguration">Base configuration</label>
                        <select class="form-control" id="baseConfiguration" name="baseConfiguration" required>
                            <#list context.configs as config>
                            <option>${config}</option>
                            </#list>
                        </select>
                    </div>
                    <div class="form-group">
                        <div class="col-auto my-1">
                            <label for="nMonths">Number of months</label>
                            <input type="string" class="form-control" required id="nMonths"
                                   value="${context.experiment.months}"
                                   name="nMonths">
                        </div>
                    </div>
                </div>

                <div class="row">
                    <legend class="scheduler-border">Additional parameters:</legend>
                    <ul class="nav nav-tabs">
                        <li class="active"><a href="#demography" aria-controls="demography" role="tab"
                                              data-toggle="tab">Demography</a>
                        </li>
                        <li><a href="#economy" aria-controls="economy" role="tab" data-toggle="tab">Economy</a></li>
                    </ul>
                    <br>
                    <div class="tab-content">
                        <div role="tabpanel" class="tab-pane active" id="demography">
                            <div class="form-group">
                                <label for="INITIAL_DEMOGRAPHY_DATA_COUNTRY">INITIAL_DEMOGRAPHY_DATA_COUNTRY</label>
                                <input type="text" class="form-control sim-property"
                                       id="INITIAL_DEMOGRAPHY_DATA_COUNTRY"
                                       name="INITIAL_DEMOGRAPHY_DATA_COUNTRY"
                                       placeholder="${context.demography['INITIAL_DEMOGRAPHY_DATA_COUNTRY']!""}"
                                       value="${context.INITIAL_DEMOGRAPHY_DATA_COUNTRY!""}"
                                >
                            </div>
                            <div class="form-group">
                                <label for="INITIAL_DEMOGRAPHY_DATA_YEAR">INITIAL_DEMOGRAPHY_DATA_YEAR</label>
                                <input type="text" class="form-control sim-property" id="INITIAL_DEMOGRAPHY_DATA_YEAR"
                                       name="INITIAL_DEMOGRAPHY_DATA_YEAR"
                                       placeholder="Year starting population data will be loaded from"
                                       value="${context.INITIAL_DEMOGRAPHY_DATA_YEAR!""}"
                                >
                            </div>
                            <#list context.demography?keys as key>
                            <div class="form-group">
                                <label for="${key}">${key}</label>
                                <input type="number" class="form-control sim-property" id="${key}" name="${key}"
                                       placeholder="${context.demography[key]}">
                            </div>
                            </#list>
                    </div>

                    <div role="tabpanel" class="tab-pane" id="economy">
                        <#list context.economy?keys as key>
                        <div class="form-group">
                            <label for="${key}">${key}</label>
                            <input type="text" class="form-control sim-property" id="${key}" name="${key}"
                                   placeholder="${context.economy[key]}">
                        </div>
                        </#list>
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


