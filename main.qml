import QtQuick 2.15
import QtQuick.Controls 2.15
import QtQuick.Layouts 1.15
import QtQuick.Controls.Material 2.15

ApplicationWindow {
    id: root

    property bool splitting: false

    width: 640
    height: 480
    visible: true
    title: qsTr("Universe Splitter Unofficial")

    header: ToolBar {
        Label {
            text: root.title
            anchors.centerIn: parent
        }
    }

    ColumnLayout {
        anchors.centerIn: parent
        spacing: 6

        Label {
            Layout.fillWidth: true
            font.pointSize: 24
            text: qsTr("Quantum Decision Maker")
            horizontalAlignment: Label.AlignHCenter
        }

        Item {
            height: 20
        }

        Label {
            text: qsTr("In one Universe, I will now:")
        }
        TextField {
            id: actionOne
            Layout.fillWidth: true
            placeholderText: qsTr("hop left")
            selectByMouse: true
            enabled: !splitting
            maximumLength: 50
        }

        Label {
            text: qsTr("In the other one, I will now:")
        }
        TextField {
            id: actionAnother
            Layout.fillWidth: true
            placeholderText: qsTr("hop right")
            selectByMouse: true
            enabled: !splitting
            maximumLength: 50
        }

        Item {
            height: 20
        }

        RoundButton {
            id: splitButton

            Material.foreground: Material.Green
            Layout.alignment: Qt.AlignHCenter

            font.pointSize: 48
            text: qsTr(" Ψ ")
            enabled: !splitting && actionOne.text.length > 0 && actionAnother.text.length > 0

            onClicked: {
                splitting = true;

                request('https://qrng.anu.edu.au/API/jsonI.php?length=1&type=uint8', function (o) {
                    if (o.status !== 200) {
                        dialog.displayDialog(qsTr("Error"), qsTr("Invalid status: '%1'").arg(o.status));
                        splitting = false;
                        return;
                    }

                    // e.g.: {"type":"uint8","length":1,"data":[68],"success":true}
                    let response= JSON.parse(o.responseText);

                    if (!response.success || !Array.isArray(response.data) || response.data.length !== 1) {
                        dialog.displayDialog(qsTr("Error"), qStr("Could not parse response: '%1'").arg(o.responseText));
                        splitting = false;
                        return;
                    }

                    let result = response.data[0] % 2 ? actionOne.text : actionAnother.text;
                    dialog.displayDialog(qsTr("Universe has been split, DO:"), result);
                    splitting = false;
                });
            }

            BusyIndicator {
                id: busy
                visible: running
                running: splitting
                anchors.fill: parent
            }
        }
    }

    InfoDialog {
        id: dialog
        anchors.fill: parent
    }

    Component.onCompleted: {
        dialog.displayRichDialog(qsTr("Universe Splitter Unofficial"),
                                 qsTr("According to the Many Worlds Interpretation of quantum mechanics, every possible outcome of a quantum experiment happens. " +
                                      "If your decision relies on the outcome of the quantum experiment, there will be many versions of you that make different decisions.<br/><br/>" +
                                      "<a href=\"http://qrng.anu.edu.au/index.php\">The ANU Quantum Random Numbers Server</a> is used to generate a random number, hence branching the universe.<br/><br/>" +
                                      "<b>Rules of the game:</b><br/>" +
                                      "<ol>" +
                                      "<li>Enter your commitments, two different things that you are willing to do.</li>" +
                                      "<li>Hit the Ψ button.</li>" +
                                      "<li>Do according whatever appears on your screen.</li>" +
                                      "</ol><br/><br/>" +
                                      "DISCLAIMER: use at your own risk, don't blame the application (or me) for your \"bad\" decisions." +
                                      "<br/><br/>This is an open source project - <a href=\"https://github.com/tomicooler/UniverseSplitterUnofficial\">GitHub</a> - feel free to contribute.<br/>"
                                     ));
    }

    function request(url, callback) {
        var req = new XMLHttpRequest();
        req.onreadystatechange = (function(res) {
            return function() {
                if (res.readyState === 4) {
                    callback(res);
                }
            }
        })(req);
        req.open("GET", url, true);
        req.send("");
    }
}
