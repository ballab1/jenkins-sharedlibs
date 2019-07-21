def call(String dir, ArrayList<String> actions) {

def cmd='''

myUpdate()
{
    branches="$(git branch)"
    ref="$(echo "$branches" | awk '{if(NF==2){print $2}}')"
    if [ "${ref:-}" ]; then
        git fetch --all --recurse-submodules
        for b in $(echo "$branches" | awk '{print substr($0,3)}'); do
            git checkout "$b"
            git reset --hard "origin/$b"
        done
        git checkout "$ref"
    fi
}

cd "''' + dir + '''"
mydir=$(pwd)
myUpdate
for d in $(git submodule status --recursive | awk '{print $2}'); do
    cd "$d"
    myUpdate
    cd "$mydir"
done

'''

actions.each{ cmd += ' ' + it }

sh cmd

}
