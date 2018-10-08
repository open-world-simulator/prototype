<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<title>OpenWorldSimulator Studio</title>

<!-- Latest compiled and minified CSS -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
      integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

<!-- Optional theme -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
      integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">

<!-- Optional theme -->
<link rel="stylesheet" href="https://cdn.datatables.net/1.10.13/css/jquery.dataTables.min.css"
      crossorigin="anonymous">

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>

<!-- Latest compiled and minified JavaScript -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
        integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
        crossorigin="anonymous"></script>


<script src="https://cdn.datatables.net/1.10.13/js/jquery.dataTables.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-csv/0.71/jquery.csv-0.71.min.js"></script>

<!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
<script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
<script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>

<![endif]-->

<style>
</style>

<script>
    var CsvToHtmlTable = CsvToHtmlTable || {};

    CsvToHtmlTable = {
        init: function (options) {

            options = options || {};
            var csv_path = options.csv_path || "";
            var el = options.element || "table-container";
            var allow_download = options.allow_download || false;
            var csv_options = options.csv_options || {};
            var datatables_options = options.datatables_options || {};
            var custom_formatting = options.custom_formatting || [];

            $("#" + el).html("<table class='table table-striped table-condensed' id='" + el + "-table'></table>");

            $.when($.get(csv_path)).then(
                    function (data) {
                        var csv_data = $.csv.toArrays(data, csv_options);

                        var table_head = "<thead><tr>";

                        for (head_id = 0; head_id < csv_data[0].length; head_id++) {
                            table_head += "<th>" + csv_data[0][head_id] + "</th>";
                        }

                        table_head += "</tr></thead>";
                        $('#' + el + '-table').append(table_head);
                        $('#' + el + '-table').append("<tbody></tbody>");

                        for (row_id = 1; row_id < csv_data.length; row_id++) {
                            var row_html = "<tr>";

                            //takes in an array of column index and function pairs
                            if (custom_formatting != []) {
                                $.each(custom_formatting, function (i, v) {
                                    var col_idx = v[0]
                                    var func = v[1];
                                    csv_data[row_id][col_idx] = func(csv_data[row_id][col_idx]);
                                })
                            }

                            for (col_id = 0; col_id < csv_data[row_id].length; col_id++) {
                                row_html += "<td>" + csv_data[row_id][col_id] + "</td>";
                            }

                            row_html += "</tr>";
                            $('#' + el + '-table tbody').append(row_html);
                        }

                        $('#' + el + '-table').DataTable(datatables_options);

                        if (allow_download)
                            $("#" + el).append("<p><a class='btn btn-info' href='" + csv_path + "'><i class='glyphicon glyphicon-download'></i> Download as CSV</a></p>");
                    });
        }
    }
</script>