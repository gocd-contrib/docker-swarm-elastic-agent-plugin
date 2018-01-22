<style>
    [data-plugin-style-id="docker-swarm-plugin"] .docker-swarm-elastic-agent {
        padding: 60px 20px 0 20px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .sub-nav {
        padding-left: 25px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .header {
        margin: 25px 0 10px 3px;
        font-weight: 600;
        text-decoration: underline;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .tabs {
        position: relative;
        clear: both;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .tabs .tab {
        float: left;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .tabs .tab .content {
    }

    [data-plugin-style-id="docker-swarm-plugin"] .checkboxtab {
        display: none;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .tab label {
        background-color: #d1c4e9;
        padding: 7px 15px;
        line-height: 19px;
        font-size: 13px;
        border-radius: 3px 3px 0 0;
        font-weight: 600;
        color: #333;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .checkboxtab:checked ~ label {
        background-color: #dfdef0;
        border: 1px solid;
        border-bottom: none;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .checkboxtab:checked ~ .content {
        z-index: 1;
        display: block !important;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .agent-name {
        padding: 10px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .tab-content.service-logs {
        background-color: #383838;
        font-size: 13px;
        font-family: monaco;
        color: white;
        display: block;
        font-weight: 400;
        line-height: 20px;
        padding-left: 10px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .pod-configuration {
        background-color: white;
        font-family: courier;
        font-size: 15px;
        white-space: pre;
        font-weight: 600;
        display: block;
        line-height: 10px;
        color: #484848;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .tab-content {
        margin-top: 0;
        height: calc(100vh - 280px);
        overflow-x: hidden;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .tab-content-outer {
        background: white;
        padding: 20px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .pod-events table {
        width: calc(100% - 40px);
        min-width: 95%;
        border: 1px solid #D8D8D8;
        font-size: 13px;
        margin: 10px 20px 20px 20px;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .pod-events table thead {
        background: #D8D8D8;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .pod-events table thead th {
        padding: 10px 20px;
        font-weight: bold;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .pod-events table tbody tr {
        border-top: 1px solid #D8D8D8;
        border-bottom: 1px solid #D8D8D8;
        border-collapse: collapse;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .pod-events table tbody td {
        padding: 10px 20px;
        max-width: 190px;
        text-overflow: ellipsis;
        overflow: hidden;
    }

    [data-plugin-style-id="docker-swarm-plugin"] dl.properties {
        overflow: hidden;
        margin: 0;
        font-size: 14px;
        vertical-align: top;
        font-family: "Open Sans", "Helvetica Neue", Helvetica, Roboto, Arial, sans-serif;
        color: #484848;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .properties dt {
        float: left;
        clear: both;
        text-align: left;
        min-width: 200px;
        font-weight: 600;
        margin: 0;
        padding: 3px;
        min-height: 20px;
        line-height: 1.5;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .properties dd {
        margin-left: 200px;
        min-height: 20px;
        padding: 3px;
        line-height: 1.5;
    }

    [data-plugin-style-id="docker-swarm-plugin"] .warning-message {
        background-color: #fdf5e2;
        color: #b88c34;
        font-weight: 600;
        font-size: 15px;
        padding: 10px 20px;
        border-radius: 3px;
        margin: 0 0 1rem 0;
        border: 1px solid rgba(10, 10, 10, 0.25);
        position: relative;
    }
</style>

<div data-plugin-style-id="docker-swarm-plugin">
    <div class="row">
        <div class="header-panel">
            <header class="page-header sub-nav">
                <f.row>
                    <f.column size={6}>
                        <h1>${ id !}</h1>
                    </f.column>
                </f.row>
            </header>
        </div>
    </div>

    <div class="content_wrapper_inner docker-swarm-elastic-agent">
        <div class="row">
            <div class="sub_tabs_container">
                <ul class="tabs" ng-init="currenttab=currentab || 'service-details'">
                    <li ng-class="{'current_tab' : currenttab == 'service-details' }"
                        ng-click="currenttab='service-details'">
                        <a href="#">Details</a>
                    </li>
                    <li ng-class="{'current_tab' : currenttab == 'service-logs' }" ng-click="currenttab='service-logs'">
                        <a href="#">Logs</a>
                    </li>
                </ul>
            </div>

            <div class="tab-content-outer">
                <div class="tab-content" ng-show="currenttab == 'service-details'">
                    <dl class="properties">
                        <dt>Service Id:</dt>
                        <dd>${ id !}</dd>
                        <dt>Created At:</dt>
                        <dd>{{ ${createdAt?long?c!} | date:"MMM dd, yyyy hh:mm:ss a"}}</dd>

                        <dt>Image:</dt>
                        <dd>${image !}</dd>
                        <dt>Command:</dt>
                        <dd>${command !}</dd>
                        <dt>Args:</dt>
                        <dd>${args !}</dd>

                        <dt>Memory Limits:</dt>
                        <dd>${limits !}</dd>
                        <dt>Memory Reservations:</dt>
                        <dd>${reservations !}</dd>
                    </dl>
                </div>
                <div class="tab-content service-logs" ng-show="currenttab == 'service-logs'">
                ${ logs?html?replace("\n", "<br>") !}
                </div>
            </div>
        </div>
    </div>
</div>


