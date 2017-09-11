<style>
    [data-plugin-style-id="docker-swarm-plugin"] .cluster {
        padding: 20px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node {
        border:        1px solid #d1c4e9;
        width:         100%;
        background:    #fff;
        border-radius: 2px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node-header {
        background:  #d1c4e9;
        padding:     10px;
        font-size:   14px;
        font-weight: 600;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node-header span {
        display:   inline-block;
        width:     15%;
        font-size: 17px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node-status::before {
        content:          '';
        display:          inline-block;
        width:            10px;
        height:           10px;
        border-radius:    5px;
        background-color: #ff0000;
        margin-right:     5px;
        vertical-align:   middle;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node-status .leader {
        font-size:   13px;
        font-weight: 600;
        color:       #7b64a7;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .node-header span.active::before {
        background-color: #14d414;
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

    [data-plugin-style-id="docker-swarm-plugin"] .warning {
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

    [data-plugin-style-id="docker-swarm-plugin"] dt:after {
        content:      ":";
        padding-left: 5px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .properties dl {
        float: left;
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
</style>

<div data-plugin-style-id="docker-swarm-plugin">
<#if nodes??>
    <#list nodes as node>
        <div class="cluster">
            <div class="node">
                <div class="node-header">
                    <div>
                        <span class="node-status ${node.availability}">
                                ${node.hostname!}
                                    <#if node.leader??>
                                        <small class="leader">[Leader]</small>
                                    </#if>
                        </span>
                    </div>
                </div>
                <div class="node-content">
                    <div>
                        <dl class="properties">
                            <dt>CPU</dt>
                            <dd>${node.cpus!}</dd>
                            <dt>Memory</dt>
                            <dd>${node.memory!}</dd>
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
                                <td colspan="7" class="warning">No running containers.</td>
                            </tr>
                            </#if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </#list>
</#if>
</div>
