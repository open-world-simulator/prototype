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
            <form method="GET" action="/save-parameters"
                  onsubmit="disableEmptyInputs(this);"
            >
                <div class="row">
                    <#include "success.ftl">
                    <legend class="scheduler-border">Experiment <strong>'${context.experiment.experimentId}'</strong> parameters:</legend>

                    <div class="form-group">
                        <label for="id">Experiment ID:</label>
                        <input type="text" class="form-control" id="id" name="id"
                               value="${context.experiment.experimentId!}"
                               required
                               readonly>
                    </div>

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
                                   required
                                   placeholder="${context.demography['INITIAL_DEMOGRAPHY_DATA_COUNTRY']!''}"
                                   value="${context.experiment.optionalProperties['INITIAL_DEMOGRAPHY_DATA_COUNTRY']!''}"
                            >
                        </div>
                        <div class="form-group">
                            <label for="INITIAL_DEMOGRAPHY_DATA_YEAR">INITIAL_DEMOGRAPHY_DATA_YEAR</label>
                            <input type="text" class="form-control sim-property" id="INITIAL_DEMOGRAPHY_DATA_YEAR"
                                   name="INITIAL_DEMOGRAPHY_DATA_YEAR"
                                   required
                                   placeholder="Year starting population data will be loaded from"
                                   value="${context.experiment.optionalProperties['INITIAL_DEMOGRAPHY_DATA_YEAR']!''}"
                            >
                        </div>
                        <#list context.demography?keys as key>
                        <div class="form-group">
                            <label for="${key}">${key}</label>
                            <input type="number" class="form-control sim-property" id="${key}" name="${key}"
                                   value="${context.experiment.optionalProperties[key]!''}"
                                   placeholder="${context.demography[key]}">
                        </div>
                        </#list>
                </div>
                <!-- TAB 2 -->
                <div role="tabpanel" class="tab-pane" id="economy">
                    <#list context.economy?keys as key>
                    <div class="form-group">
                        <label for="${key}">${key}</label>
                        <input type="text" class="form-control sim-property" id="${key}" name="${key}"
                               placeholder="${context.economy[key]}">
                    </div>
                    </#list>
                </div>
                <!-- TAB 3 -->
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


