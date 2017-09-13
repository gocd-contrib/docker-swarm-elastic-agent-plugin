<style>
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

    [data-plugin-style-id="docker-swarm-plugin"] .node-header div:first-child {
        padding-left: 0px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node-header span {
        display:   inline-block;
        width:     15%;
        font-size: 17px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node-content {
        padding: 10px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node table {
        width:     calc(100% - 40px);
        min-width: 95%;
        border:    1px solid #D8D8D8;
        font-size: 13px;
        margin:    10px 20px 20px 20px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node table thead {
        background: #D8D8D8;
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

    [data-plugin-style-id="docker-swarm-plugin"] .node table tbody td.status {
        width: 160px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node table tbody td.service-name {
        width: 260px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .message {
        font-size:   13px;
        font-weight: 600;
    }

    [data-plugin-style-id="docker-swarm-plugin"] dl.properties {
        overflow:  hidden;
        margin:    0;
        font-size: 13px;
        display:   inline-block;
    }

    [data-plugin-style-id="docker-swarm-plugin"] dt {
        float:       left;
        clear:       both;
        min-width:   150px;
        text-align:  right;
        font-weight: normal;
        padding:     5px;
        margin:      0;
    }

    [data-plugin-style-id="docker-swarm-plugin"] dl.properties.inline dt {
        clear: none;
    }

    [data-plugin-style-id="docker-swarm-plugin"] dl.properties.inline dd, [data-plugin-style-id="docker-swarm-plugin"] dl.properties.inline dt {
        padding: 0px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] dl.properties.inline dt:first-child {
        min-width:   auto;
        margin-left: 10px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] dt:after {
        content:      ":";
        padding-left: 5px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] dd {
        float:       left;
        padding:     5px;
        font-weight: 600;
        margin:      0;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node .header {
        margin: 20px 0px 0px 20px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node-header .accordian-icon {
        width: 0px;
        float: right;
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
</style>

<div data-plugin-style-id="docker-swarm-plugin">
    <div class="cluster">
    <#if nodes?size != 0>
        <#list nodes as node>
            <#assign nodeIndex = node?index>
            <#assign ngModel = "node${nodeIndex}">
            <div class="node">
                <div class="node-header row" ng-click="${ngModel} = !${ngModel}" ngModel="${ngModel}"
                     ng-value="false" ng-init="${ngModel} = ${(nodeIndex == 0)?then('true','false')}">
                    <div class="columns medium-11">
                        <i class="fa fa-desktop" aria-hidden="true"></i>
                        <dl class="properties inline">
                            <dt>Hostname</dt>
                            <dd>${node.hostname!}</dd>
                            <dt>Node Id</dt>
                            <dd>${node.id!}</dd>
                            <dt>Leader</dt>
                            <dd>
                                <#if node.leader??>
                                    Yes
                                <#else>
                                    No
                                </#if>
                            </dd>
                            <dt>Status</dt>
                            <dd>${node.availability!}</dd>
                        </dl>
                    </div>
                    <div class="columns accordian-icon">
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
                            <dd>${node.memory!} MB</dd>
                            <dt>IP Address</dt>
                            <dd>${node.nodeIP!}</dd>
                        </dl>

                        <dl class="properties">
                            <dt>Role</dt>
                            <dd>${node.role!}</dd>
                            <dt>Reachable</dt>
                            <dd>${node.reachability!}</dd>
                            <dt>Architecture</dt>
                            <dd>${node.architecture!}</dd>
                        </dl>

                        <dl class="properties">
                            <dt>Updated At</dt>
                            <dd>{{ ${node.updatedAt?long?c!} | date:"MMM dd, yyyy hh:mm:ss a"}}</dd>
                            <dt>Created At</dt>
                            <dd>{{ ${node.createdAt?long?c!} | date:"MMM dd, yyyy hh:mm:ss a"}}</dd>
                            <dt>Docker version</dt>
                            <dd>${node.engineVersion!}</dd>
                        </dl>
                    </div>
                    <h4 class="header">Running Containers</h4>
                    <table>
                        <thead>
                        <tr>
                            <th>Id</th>
                            <th>Image</th>
                            <th>State</th>
                            <th>Status</th>
                            <th>Service Name</th>
                        </thead>
                        </tr>
                        <tbody>
                            <#if node.containers?size != 0>
                                <#list node.containers as container>
                                <tr>
                                    <td ng-click="document.execCommand('copy')">${container.id!}</td>
                                    <td>${container.image!}</td>
                                    <td class="state">${container.state!}</td>
                                    <td class="status">${container.status!}</td>
                                    <td class="service-name">${container.serviceName!}</td>
                                </tr>
                                </#list>
                            <#else>
                            <tr>
                                <td colspan="7" class="message">No running containers.</td>
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
