import QtQuick 2.15
import QtQuick.Controls 2.15

Item {

    property string content: ""

    function displayRichDialog(title, details) {
        richDialog.title = title;
        content = details;
        richDialog.open();
    }

    function displayDialog(title, details) {
        dialog.title = title;
        content = details.substring(0, 50);
        dialog.open();
    }

    Dialog {
        id: dialog

        width: parent.width * 0.8
        height: parent.height * 0.4
        x: (parent.width - width) / 2
        y: (parent.height - height) / 2

        standardButtons: Dialog.Ok

        Label {
            anchors.centerIn: parent
            text: content
            wrapMode: Label.Wrap
            verticalAlignment: Label.AlignVCenter
            textFormat: Text.PlainText
            font.pointSize: 24
        }
    }

    Dialog {
        id: richDialog

        width: parent.width * 0.8
        height: parent.height * 0.8
        x: (parent.width - width) / 2
        y: (parent.height - height) / 2

        standardButtons: Dialog.Ok

        ScrollView {
            id: scroll
            anchors.fill: parent
            anchors.margins: 10
            contentWidth: label.width
            clip: true

            Label {
                id: label
                width: dialog.width - 80
                text: content
                wrapMode: Label.Wrap
                verticalAlignment: Label.AlignVCenter
                textFormat: Text.RichText

                onLinkActivated: {
                    Qt.openUrlExternally(link);
                }
            }
        }
    }
}
