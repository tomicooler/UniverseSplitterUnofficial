import QtQuick 2.15
import QtQuick.Controls 2.15

Item {

    function displayDialog(title, details) {
        dialog.title = title;
        dialog.details = details;
        dialog.open();
    }

    Dialog {
        id: dialog

        width: parent.width * 0.8
        height: parent.height * 0.8
        x: (parent.width - width) / 2
        y: (parent.height - height) / 2

        standardButtons: Dialog.Ok
        property string details: ""

        ScrollView {
            id: scroll
            anchors.fill: parent
            anchors.margins: 10
            contentWidth: label.width
            clip: true

            Label {
                id: label
                width: dialog.width - 80
                text: dialog.details
                wrapMode: Label.Wrap
                verticalAlignment: Label.AlignVCenter

                onLinkActivated: {
                    Qt.openUrlExternally(link);
                }

                Component.onCompleted: {
                    let minHeight = dialog.height - 80;
                    if (height < minHeight) {
                        height = minHeight;
                    }
                }
            }
        }

    }

}
