import QtQuick 2.15
import QtQuick.Controls 2.15

ApplicationWindow {
    width: 640
    height: 480
    visible: true
    title: qsTr("Universe Splitter Unofficial")

    ScrollView {
        anchors.fill: parent

        ListView {
            width: parent.width
            model: 20
            delegate: ItemDelegate {
                text: "Item " + (index + 1)
                width: parent.width
            }
        }
    }

    InfoDialog {
        id: dialog
        anchors.fill: parent
    }

    Component.onCompleted: {
        dialog.displayDialog(qsTr("Universe Splitter Unofficial"),
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
}
