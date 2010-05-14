<html>
<head>
<script type="text/javascript">

function invoke(id, name, op) {
    var params = "?op=" + op + "&name=" + name + "&id=" + id
    var argsid = name + "." + op + ".args"
    var rsltid = name + ".invokeResult"
    var args = dojo.byId(argsid)
    if (args) {
        params += "&args=" + args.value
    }
    dojo.io.bind({
      url:  '/<%= urlFor(action:"invoke") %>' + params,
      method: "post",
      mimetype:  "text/json-comment-filtered",
      load:  function(type, data, evt) {
        dojo.byId(rsltid).innerHTML = data.html;
      },
    });
  }

function updateFilters(id) {
<%  for (filter in filters) { %>
    if ("${filter.'@id'}" == id) {
      dojo.byId('pattern').value = "${filter.objectName.text()}"
      dojo.byId('attributeFilter').value = "${filter.attributeRegex.text()}"
      dojo.byId('operationFilter').value = "${filter.operationRegex.text()}"
    }
<% } %>
}

dojo.addOnLoad(function(){
<% if (message) { %>
    dojo.byId('queryResult').innerHTML = "${message}";
<% } else { %>
    dojo.byId('queryResult').innerHTML = '';
<% } %>
setRefreshInterval(${refreshInterval});
});

function refreshAttributeData() {
    if (dojo.byId('mbeans').childNodes.length > 1) {
         dojo.byId('queryResult').innerHTML = '... updating';
         dojo.io.bind({
          url: '<%= urlFor(action:"listMBeans") %>',
          method: "get",
          mimetype: "text/json-comment-filtered",
          content: {
              eid: "${eid}",
              pattern: dojo.byId("pattern").value,
              attributeFilter: dojo.byId("attributeFilter").value,
              operationFilter: dojo.byId("operationFilter").value,
              presetFilter: dojo.byId("presetFilter").value
          },
          load: function(type, data, evt) {
                dojo.byId('queryResult').innerHTML =  "${message}";
                var res = data.results;
                for (var i=0; i < res.length; i++) {
                  var r = res[i];
                  if (r.value) {
                    dojo.byId(r.id).innerHTML = r.value;
                  }
                }
            }
        });
      }
}

var refreshIntervalId = 0;
function setRefreshInterval(val) {
  if (val == 0) {
    clearInterval(refreshIntervalId);
  }
  else {
    clearInterval(refreshIntervalId);
    refreshIntervalId = setInterval("refreshAttributeData()", 60 * 1000 * val);
  }
}
</script>

<style type="text/css">
input[type=text] {
    border: 1px solid gray;
    width: 500px;
    padding: 3px
}
</style>
</head>
<body>

<form name="MBeanQuery" method="POST">
<table width="90%">
<tr><td width="15%">Object Name Pattern:</td>
<td width="60%"><input name="pattern" id="pattern" type="text" value="${pattern}"/></td>
<td>Preset Searches:</td>
<td><select id="presetFilter" name="presetFilter" style="width:160px" onchange="updateFilters(this.options[this.selectedIndex].value);" >
<% for (filter in filters) { %>
 <option value ="${filter.'@id'}"<% if(presetFilter == filter.'@id') { %> selected<% } %>>${filter.'@id'}</option>
 <% } %>
</select>
</td></tr>
<tr><td>Attribute Regex Filter:</td>
<td><input name="attributeFilter" id="attributeFilter" type="text" value="${attributeFilter}"/></td>
<td>Refresh Interval:</td>
<td><select id="refreshInterval" name="refreshInterval" style="width:100px" onchange="setRefreshInterval(this.options[this.selectedIndex].value);" >
  <option value ="0"<% if(refreshInterval == "0") { %> selected<% } %>>Off</option>
 <option value ="1"<% if(refreshInterval == "1") { %> selected<% } %>>1 min</option>
  <option value ="5"<% if(refreshInterval == "5") { %> selected<% } %>>5 min</option>
</select></td></tr>
<tr><td>Operation Regex Filter:</td>
<td><input name="operationFilter" id="operationFilter" type="text" value="${operationFilter}"/></td>
<td></td>
</tr>
</table>
<br/>
<table width="90%">
<tr><td width="15%"><input type="submit" value="Query MBeans"/></td>
<td width="60%"><div id="queryResult"/></td><td>&nbsp;</td></table>
</form>


<div id="mbeans">
<% for (result in data) { %>
   <% def resource = result['resource'] %>
   <% if (resource) { %>
      <%= linkTo(resource.name, [resource:resource]) %>
   <% } %>
   <% for (bean in result['beans']) { %>
      <h3>${bean.name}</h3>
      <% if (bean.attrNames.length > 0) { %>
      <h4>Attributes:</h4>
      <table width="95%" border="1">
	<thead><tr><td width="30%"><b>Name</b></td><td><b>Value</b></td></tr></thead>
         <% for (attrName in bean.attrNames) { %>
            <tr><td>${attrName}</td>
		<td><div id="${bean.dojoId}.${attrName}.value" style="overflow:auto"><%= bean.attrs.get(attrName)['Value'] %></div></td>
            </tr>
         <% } %>
     </table>
      <% } %>

      <% if (bean.ops.size > 0) { %>
         
	<h4>Operations:</h4>
      <table width="95%" border="1">
	<tr><td width="100%">
         <% for (op in bean.ops) { %>
            <% if (op.signature.length < 2) { %>
                <% if (op.signature.length > 0 && bean.ops.size > 1) { %>
                   <br/>
                <% } %>
                <a class="buttonGreen" onclick="invoke(${bean.resId}, '${bean.name}', '${op.name}')" href="javascript:void(0)"><span>${op.name}</span></a>
                <% for (def i=0; i<op.signature.length; i++) { %>
                   <input style="width:75px" type="text" id="${bean.name}.${op.name}.args"/>
                <% } %>
              <% } %>  
         <% } %>
         </td></tr>
	</table>
	 <table border="0"><td><tr><div id="${bean.name}.invokeResult" /></tr></td></table>
      <% } %>
   <% } %>
<% } %>
</div>


</body>
</html>