#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Starts XAP management (GSM) and network discovery (LUS) components"
    echo ""
    echo "Usage: $0 [--help]"
    echo ""
}

parse_input() {
    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ $# -gt 0 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi

    if [[ -z "$JSHOMEDIR" ]]; then
        echo "Please set environment variable JSHOMEDIR"
        exit 1
    fi

    if [[ -z "$JAVA_HOME" ]]; then
        echo "Please set environment variable JAVA_HOME"
        exit 1
    fi
}

start_mgt() {
    export GS_HOME=${JSHOMEDIR}
    export PATH=${JSHOMEDIR}/bin:${PATH}
    export GSA_JAVA_OPTIONS="$GSA_JAVA_OPTIONS -Dprocess.marker=management-agent-marker"

    readonly log_dir="${JSHOMEDIR}/logs"
    mkdir -p ${log_dir}

    readonly log_file="${log_dir}/start-mgt.log"
    if [[ -e "${log_file}" ]]; then
        readonly script_mod_date=$(date +%Y-%m-%d~%H.%M.%S -r ${log_file})
        readonly log_file_zip="${log_dir}/${script_mod_date}-start-mgt.zip"

        zip ${log_file_zip} ${log_file}
    fi

    nohup ${JSHOMEDIR}/bin/gs-agent.sh gsa.global.lus 0 gsa.lus 1 \
        gsa.global.gsm 0 gsa.gsm 1 gsa.gsc 0 >${log_file} 2>&1 &
    echo "Starting 1 GSM and 1 LUS... See ${log_file}"
}

main() {
    parse_input "$@"
    start_mgt
}

main "$@"
