<style>
	.main-container {
		margin-top: 0;
	}

	#status_reports {
		margin-top: 50px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .cluster {
		padding: 20px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node {
		border:        1px solid #d1c4e9;
		width:         100%;
		background:    #fff;
		border-radius: 2px;
		margin-bottom: 20px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node-header {
		background:  #d1c4e9;
		padding:     10px;
		font-size:   14px;
		font-weight: 600;
		cursor:      pointer;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node-header .left {
		float:   left;
		padding: 0px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node-header .right {
		float: right;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node-header div:first-child {
		padding-left: 0px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node-header span {
		display:   inline-block;
		width:     15%;
		font-size: 17px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node-content {
		padding: 25px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node table {
		width:     100%;
		border:    1px solid #D8D8D8;
		margin:    10px 0px 0px 0px;
		font-size: 13px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node table thead {
		background: #b9f6ca;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node table thead th {
		padding:     10px 20px;
		font-weight: bold;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node table tbody tr {
		border-top:      1px solid #D8D8D8;
		border-bottom:   1px solid #D8D8D8;
		border-collapse: collapse;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node table tbody td {
		padding:       10px 20px;
		max-width:     190px;
		text-overflow: ellipsis;
		overflow:      hidden;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node table tbody td.state {
		width: 50px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node table tbody td.task-created {
		width: 160px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node table tbody td.service-id {
		width: 260px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .message {
		font-size:   13px;
		font-weight: 600;
	}

	[data-plugin-style-id="docker-swarm-plugin"] dl.properties {
		overflow:       hidden;
		margin:         0;
		font-size:      13px;
		display:        inline-block;
		vertical-align: top;
	}

	[data-plugin-style-id="docker-swarm-plugin"] dt {
		float:       left;
		clear:       both;
		font-weight: 600;
		padding:     5px 0px;
		margin:      0;
	}

	[data-plugin-style-id="docker-swarm-plugin"] dl.properties.inline dt {
		clear:          none;
		min-width:      auto;
		vertical-align: bottom;
	}

	[data-plugin-style-id="docker-swarm-plugin"] dl.properties.inline dd, [data-plugin-style-id="docker-swarm-plugin"] dl.properties.inline dt {
		padding: 0px;
		height:  15px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] dl.properties.inline dd {
		margin-right: 15px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node-header .hostname, [data-plugin-style-id="docker-swarm-plugin"] .node-header .node-id {
		min-width:     190px;
		max-width:     190px;
		overflow:      hidden;
		text-overflow: ellipsis;
	}

	@media screen and (max-width: 1366px) {
		[data-plugin-style-id="docker-swarm-plugin"] .node-header .hostname, [data-plugin-style-id="docker-swarm-plugin"] .node-header .node-id {
			max-width:     150px;
			min-width:     150px;
			overflow:      hidden !important;
			text-overflow: ellipsis !important;
		}
	}

	@media screen and (max-width: 1280px) {
		[data-plugin-style-id="docker-swarm-plugin"] .node-header .hostname, [data-plugin-style-id="docker-swarm-plugin"] .node-header .node-id {
			max-width:     100px;
			min-width:     50px;
			overflow:      hidden !important;
			text-overflow: ellipsis !important;
		}
	}

	[data-plugin-style-id="docker-swarm-plugin"] dl.properties.inline dt:first-child {
		min-width:   auto;
		margin-left: 10px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] dt:after {
		content: ":";
		padding: 0px 5px 0px 2px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] dd {
		float:        left;
		padding:      5px 0px;
		font-weight:  normal;
		margin-right: 50px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .node .header {
		margin: 20px 0px 0px 0px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .warning {
		font-size:     13px;
		font-weight:   600;
		margin-bottom: 10px;
		border-left:   2px solid #f7bc08;
		padding:       10px;
		background:    #fff;
		border-radius: 2px;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .status-report-page-header {
		top:             40px;
		width:           100%;
		height:          50px;
		display:         flex;
		z-index:         10;
		position:        fixed;
		background:      #fff;
		box-shadow:      0 0 0 #000;
		border-bottom:   1px solid #ddd;
		background-size: cover;
	}

	[data-plugin-style-id="docker-swarm-plugin"] .page_title {
		font-size:   17px;
		font-weight: 600;
		margin:      0;
		padding:     0 30px;
		align-self:  center;
	}
</style>

<div data-plugin-style-id="docker-swarm-plugin">
	<div class="status-report-page-header">
		<h1 class="page_title">Docker Swarm Cluster Status Report</h1>
	</div>
	<div class="cluster">
    <#if nodes?size != 0>
        <#list nodes as node>
            <#assign nodeIndex = node?index>
            <#assign ngModel = "node${nodeIndex}">
            <div class="node">
				<div class="node-header row" ng-click="${ngModel} = !${ngModel}" ngModel="${ngModel}"
					 ng-value="false" ng-init="${ngModel} = true">
					<div class="columns medium-10 left">
						<i class="fa fa-desktop" aria-hidden="true"></i>
						<dl class="properties inline">
							<dt>Hostname</dt>
							<dd class="hostname">${node.hostname!}</dd>
							<dt>Node Id</dt>
							<dd class="node-id">${node.id!}</dd>
							<dt>Role</dt>
							<dd class="role">${node.role!}</dd>
							<dt>Status</dt>
							<dd class="status">${node.state!}</dd>
							<dt>Availability</dt>
							<dd class="availability">${node.availability!}</dd>
                            <#if node.managerStatus??>
                                <dt>Manager Status</dt>
                                <dd class="leader">${node.managerStatus}</dd>
                            </#if>
						</dl>
					</div>
					<div class="right">
						<dl class="properties inline" style="margin-right: 10px">
							<dt>Task count</dt>
							<dd>${node.tasks?size}</dd>
						</dl>
						<i class="fa fa-chevron-down" aria-hidden="true" ng-show="${ngModel}"></i>
						<i class="fa fa-chevron-right" aria-hidden="true" ng-hide="${ngModel}"></i>
					</div>
				</div>
				<div class="node-content" ng-show="${ngModel}">
					<div>
						<dl class="properties">
							<dt>CPU</dt>
							<dd>${node.cpus!}</dd>
							<dt>Memory</dt>
							<dd>${node.memory!}</dd>
						</dl>

						<dl class="properties">
							<dt>OS</dt>
							<dd>${node.os!}</dd>
							<dt>Architecture</dt>
							<dd>${node.architecture!}</dd>
						</dl>

						<dl class="properties">
							<dt>IP Address</dt>
							<dd>${node.nodeIP!}</dd>
							<dt>Docker version</dt>
							<dd>${node.engineVersion!}</dd>
						</dl>
					</div>
					<h4 class="header">Running tasks</h4>
					<table>
						<thead>
						<tr>
							<th>Id</th>
							<th>Job Identifier</th>
							<th>Image</th>
							<th>State</th>
							<th>Created At</th>
							<th>Service Id</th>
						</thead>
						</tr>
						<tbody>
                            <#if node.tasks?size != 0>
                                <#list node.tasks as task>
                                <tr>
									<td>${task.id!}</td>
									<td>
									<#if task.jobIdentifier??>
										<a rel="nofollow noreferrer" target="_blank"
										   href="/go/admin/status_reports/cd.go.contrib.elastic-agent.docker-swarm/agent/${task.serviceId!}?job_id=${task.jobIdentifier.jobId}">${task.jobIdentifier.representation!}</a>
                                    </#if>
									</td>
									<td>${task.image!}</td>
									<td>${task.state!}</td>
									<td>{{ ${task.created?long?c!} | date:"MMM dd, yyyy hh:mm:ss a"}}</td>
									<td>${task.serviceId!}</td>
								</tr>
                                </#list>
                            <#else>
                            <tr>
								<td colspan="6" class="message">No running tasks.</td>
							</tr>
                            </#if>
						</tbody>
					</table>
				</div>
			</div>
        </#list>
    <#else>
        <div class="warning">No node in cluster.</div>
    </#if>
	</div>
</div>
